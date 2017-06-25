package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Service to support runnable feature
 */
public abstract class AbstractRunningService {

    protected volatile boolean active = false;

    private final Lock stateChangeLock = new ReentrantLock();
    private final Phaser runtimePhaser = new Phaser(2);

    @Autowired
    @Qualifier("serviceRunner")
    private ExecutorService executor;

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

            getLogger().debug("Execute pre-start method");
            beforeStart();

            getLogger().debug("Starting main service loop");
            executor.submit(() -> serviceLoop());
            int phase = runtimePhaser.arriveAndAwaitAdvance();
            getLogger().debug("Starting service phase = {}...", phase);

            getLogger().debug("Execute post-start method");
            afterStart();

        } catch (Exception ex) {
            getLogger().error("Start sequence threw", ex);
        } finally {
            stateChangeLock.unlock();
        }
    }

    /**
     * To shutdown the loop of service
     *
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
            int phase = runtimePhaser.arriveAndAwaitAdvance();
            getLogger().debug("Stopped main-loop phase = {}...", phase);

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
    public boolean isActive() {
        return active;
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
    protected void serviceLoopException(Throwable t) {}

    /**
     * To update configured parameter in bean
     *
     * @param changes received new configuration
     * @param fullName canonical (with path) name of item
     * @param ubdatByItem function to do for particular item
     */
    protected void updateParameter(
            final Map<String, ConfiguredVariableItem> changes,
            final String fullName,
            final Consumer<ConfiguredVariableItem> ubdatByItem) {
        final ConfiguredVariableItem item;
        if ((item = changes.get(fullName)) != null) {
            ubdatByItem.accept(item);
        }
    }

    // private methods
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
            getLogger().error("Cautch error", t);
            serviceLoopException(t);
        } finally {
            getLogger().info("Service main loop is finished.");
            active = false;
            int phase = runtimePhaser.arrive();
            getLogger().debug("Finished service phase = {}...", phase);
        }
    }
}
