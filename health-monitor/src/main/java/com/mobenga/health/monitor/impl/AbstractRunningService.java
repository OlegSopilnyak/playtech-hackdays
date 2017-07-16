package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.model.business.ModuleHealth;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Service to support runnable feature
 */
public abstract class AbstractRunningService implements ModuleHealth {

    private volatile Condition condition = Condition.VERY_GOOD;
    private volatile ScheduledFuture scanFuture;
    protected volatile boolean active = false, firstTime = true;

    private final Lock stateChangeLock = new ReentrantLock();
    private final Phaser runtimePhaser = new Phaser(2);
    private final LruCache<Long, Object> errors = new LruCache<>(Condition.values().length);

    @Autowired
    @Qualifier("serviceRunner")
    private ScheduledExecutorService executor;

    /**
     * Return a delay between run iterations
     *
     * @return the value
     */
    protected abstract long scanDelayMillis();

    /**
     * To start the loop of service
     */
    public void start() {
        if (active) {
            getLogger().warn("Service started already.");
            return;
        }
        try {
            stateChangeLock.lock();
            getLogger().info("Starting service...");
            firstTime = true;
            errors.clear();
            condition = Condition.VERY_GOOD;

            getLogger().debug("Execute pre-start method");
            beforeStart();

            getLogger().debug("Starting main service loop");
            scanFuture = executor.scheduleWithFixedDelay(()->moduleServiceIteration(), 0, scanDelayMillis(), TimeUnit.MILLISECONDS);
            int phase = runtimePhaser.arriveAndAwaitAdvance();
            getLogger().debug("Starting service phase = {}...", phase);

            getLogger().debug("Execute post-start method");
            afterStart();

        } catch (Throwable ex) {
            getLogger().error("Start sequence threw", ex);
        } finally {
            stateChangeLock.unlock();
        }
    }

    /**
     * To shutdown the loop of service
     */
    public void shutdown() {
        if (!active) {
            getLogger().warn("Service stopped already.");
            return;
        }

        try {
            stateChangeLock.lock();
            getLogger().debug("Execute pre-stop method");
            beforeStop();

            active = false;
            getLogger().info("Stopping service...");
            scanFuture.cancel(true);
            getLogger().debug("Execute post-stop method");
            afterStop();
        } catch (Exception ex) {
            getLogger().error("Stop sequence threw", ex);
        } finally {
            stateChangeLock.unlock();
        }
    }

    /**
     * getter for "active" attribute
     *
     * @return true if main loop is run
     */
    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * To get the health condition of module for the moment
     *
     * @returnn current condition value
     */
    @Override
    public Condition getCondition() {
        return condition;
    }

    /**
     * To get last throwable object
     *
     * @return mistake or null if none
     */
    @Override
    public Throwable getLastMistake() {
        final Optional<Throwable> mistake = errors.values()
                .stream().filter(v -> v instanceof  Throwable).map(v -> (Throwable)v).findFirst();
        return mistake.isPresent() ? mistake.get() : null;
    }

    /**
     * To get concrete logger instance
     *
     * @return concrete service logger
     */
    protected abstract Logger getLogger();

    /**
     * To do service related things before main loop starts
     */
    protected abstract void beforeStart();

    /**
     * To do service related things after main loop starts
     */
    protected abstract void afterStart();

    /**
     * To do service related actions before main loop stops
     */
    protected abstract void beforeStop();

    /**
     * To do service related actions after main loop stops
     */
    protected abstract void afterStop();

    /**
     * Execute one iteration of service main loop
     *
     * @throws java.lang.InterruptedException if appropriate signal received
     */
    protected abstract void serviceLoopIteration() throws InterruptedException;

    /**
     * Execute action in exceptional situation
     *
     * @param t exception
     */
    protected void serviceLoopException(Throwable t) {
    }

    /**
     * To update configured parameter in bean
     *
     * @param changes      received new configuration
     * @param fullName     canonical (with path) name of configured item
     * @param updateByItem function to do for particular item
     */
    protected void updateParameter(
            final Map<String, ConfiguredVariableItem> changes,
            final String fullName,
            final Consumer<ConfiguredVariableItem> updateByItem) {
        final ConfiguredVariableItem item = changes.get(fullName);
        if (item != null) {
            updateByItem.accept(item);
        }
    }

    // private methods
    private void moduleServiceIteration() {
        if (firstTime) {
            active = true;
            int phase = runtimePhaser.arriveAndAwaitAdvance();
            firstTime = false;
            getLogger().debug("Started service phase = {}...", phase);
        }
        try {
            if (active) {
                getLogger().debug("Execute iteration of main loop.");
                serviceLoopIteration();
                healthBetter();
            }
        } catch (Throwable ex) {
            getLogger().error("Caught error", ex);
            if (!active) {
                return;
            }
            serviceLoopException(ex);
            if (condition == Condition.FAIL) {
                shutdown();
            }
            healthWorse(ex);
        }
    }

    private void healthWorse(Throwable ex) {
        final int state = condition.ordinal();
        final Condition[] conditions = Condition.values();
        condition = conditions[state == conditions.length - 1 ? state : state + 1];
        errors.put(System.currentTimeMillis(), ex);
    }

    private void healthBetter() {
        final int state = condition.ordinal();
        final Condition[] conditions = Condition.values();
        condition = conditions[state == 0 ? state : state - 1];
        errors.put(System.currentTimeMillis(), "Success");
    }

    // Main loop method
    private void serviceLoop() {
        active = true;
        try {
            int phase = runtimePhaser.arriveAndAwaitAdvance();
            getLogger().debug("Started service phase = {}...", phase);
            while (active) {
                getLogger().debug("Working service phase = {}...", runtimePhaser.getPhase());
                if (active) {
                    getLogger().debug("Execute iteration of main loop.");
                    serviceLoopIteration();
                }
            }
        } catch (Throwable t) {
            getLogger().error("Caught error", t);
            serviceLoopException(t);
        } finally {
            getLogger().info("Service main loop is finished.");
            active = false;
            int phase = runtimePhaser.arrive();
            getLogger().debug("Finished service phase = {}...", phase);
        }
    }

    // inner classes
    private class LruCache<A, B> extends LinkedHashMap<A, B> {
        private final int maxEntries;

        public LruCache(final int maxEntries) {
            super(maxEntries + 1, 1.0f, true);
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
            return super.size() > maxEntries;
        }
    }
}
