package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
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

    private CountDownLatch stateChangeKeeper;
    protected volatile boolean active = false;
    private final Lock stateChangeLock = new ReentrantLock();
    
    @Autowired
    @Qualifier("serviceRunner")
    private ExecutorService executor;

    /**
     * To start the loop of service
     */
    public void start() {
        try {
            stateChangeLock.lock();
            if (active) {
                getLogger().warn("Service started already.");
                return;
            }
            getLogger().info("Starting service...");

            getLogger().debug("Execute pre-start method");
            beforeStart();

            getLogger().debug("Starting main service loop");
            stateChangeKeeper = new CountDownLatch(1);
            executor.submit(() -> serviceLoop());
            stateChangeKeeper.await();

            getLogger().debug("Execute post-start method");
            afterStart();
        } catch (InterruptedException ex) {
            getLogger().error("State change keeper threw", ex);
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
    public void shutdown(){
        try{
            stateChangeLock.lock();
            if (!active) {
                getLogger().warn("Service stopped already.");
                return;
            }
            
            getLogger().debug("Execute pre-stop method");
            beforeStop();

            getLogger().info("Stopping service...");
            active = false;
            stateChangeKeeper.await();
            
            getLogger().debug("Execute post-stop method");
            afterStop();
        } catch (InterruptedException ex) {
            getLogger().error("State change keeper threw", ex);
        } catch (Exception ex) {
            getLogger().error("Stop sequence threw", ex);
        }finally{
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
     * Execute iteration of service main loop
     */
    protected abstract void serviceLoopIteration();
    /**
     * Execute action in exceptional situation
     * @param t exception
     */
    protected void serviceLoopException(Throwable t){}
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
        stateChangeKeeper.countDown();
        getLogger().info("Service main loop started.");
        stateChangeKeeper = new CountDownLatch(1);
        try {
            while (active) {
                getLogger().debug("Execute iteration of main loop.");
                serviceLoopIteration();
            }
        }catch(Throwable t){
            getLogger().error("Cautch error", t);
            serviceLoopException(t);
        } finally {
            getLogger().info("Service main loop is finished.");
            active = false;
            stateChangeKeeper.countDown();
        }
    }
}
