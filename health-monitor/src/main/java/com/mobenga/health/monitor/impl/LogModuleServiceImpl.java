package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.LogMessage;
import com.mobenga.health.model.ModuleOutput;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.health.model.factory.impl.ModuleOutputDeviceFactory;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.storage.ModuleOutputStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The service handles module's output type "log"
 */
public class LogModuleServiceImpl implements ModuleOutput.DeviceFactory, MonitoredService {
    private static final Logger LOG = LoggerFactory.getLogger(LogModuleServiceImpl.class);

    @Autowired
    @Qualifier("serviceRunner")
    private ExecutorService executor;
    @Autowired
    private UniqueIdGenerator idGenerator;

    private final BlockingQueue<Event> eventsQueue = new LinkedBlockingQueue<>();

    @Autowired
    private ModuleOutputStorage storage;

    @Autowired
    private ModuleStateNotificationService notifier;

    private final AtomicBoolean serviceWorks = new AtomicBoolean(false);

    public void startService(){
        if (serviceWorks.get()) return;
        LOG.info("Starting service");
        executor.submit(()->processEvents());
        while (!serviceWorks.get());
        // register the device
        ModuleOutputDeviceFactory.registerDeviceFactory(this);
        // register the module
        notifier.register(this);
    }

    public void stopService(){
        if (!serviceWorks.get()) return;
        LOG.info("Stopping service");
        eventsQueue.offer(Event.NULL);
        while (!serviceWorks.get());
        LOG.info("Service stopped");
        // un register the module
        notifier.unRegister(this);
    }
    /**
     * To create the Device for module's output
     *
     * @param module
     * @return the instance
     */
    @Override
    public ModuleOutput.Device create(HealthItemPK module) {
        return new LogDevice(module);
    }

    /**
     * returns supported type of ModuleOutput
     *
     * @return the value
     */
    @Override
    public String getType() {
        return LogMessage.OUTPUT_TYPE;
    }

    // private methods
    private void processEvents(){
        serviceWorks.getAndSet(true);
        try{
            while (serviceWorks.get()){
                final Event event = eventsQueue.take();
                if (Event.NULL == event){
                    LOG.debug("Received signal to stop service.");
                    break;
                }
                saveEvent(event);
            }
        } catch (InterruptedException e) {
            LOG.error("Events queue iterrupted", e);
        } catch (Throwable t) {
            LOG.error("Caught unexpected exception", t);
        } finally {
            serviceWorks.getAndSet(false);
            LOG.info("Service stopped");
        }
    }

    private void saveEvent(Event event) {
        final LogMessage message = (LogMessage) storage.createModuleOutput(event.module, LogMessage.OUTPUT_TYPE);
        message.setId(idGenerator.generate());
        message.setActionId(event.actionId);
        final StringBuilder msg = new StringBuilder();
        for(Object arg : event.arguments) msg.append(arg);
        message.setPayload(msg.toString());
        storage.saveModuleOutput(message);
    }

    /**
     * To get the value of Module's PK
     *
     * @return value of PK (not null)
     */
    @Override
    public HealthItemPK getModulePK() {
        return this;
    }

    /**
     * Describe the state of module
     *
     * @return true if module active
     */
    @Override
    public boolean isActive() {
        return serviceWorks.get();
    }

    /**
     * The handle to restart monitored service
     */
    @Override
    public void restart() {
        if (isActive()){
            stopService();
        }
        startService();
    }

    /**
     * to get the value of item's system
     *
     * @return the value
     */
    @Override
    public String getSystemId() {
        return "healthMonitor";
    }

    /**
     * to get the value of item's application
     *
     * @return the value
     */
    @Override
    public String getApplicationId() {
        return "modulesOuputService";
    }

    /**
     * to get the value of item's application version
     *
     * @return the value
     */
    @Override
    public String getVersionId() {
        return "0.01";
    }

    /**
     * to get description of module
     *
     * @return the value
     */
    @Override
    public String getDescription() {
        return "Module for log-messages";
    }

    /**
     * To get current configuration of module
     *
     * @return the map
     */
    @Override
    public Map<String, ConfiguredVariableItem> getConfiguration() {
        return Collections.EMPTY_MAP;
    }

    /**
     * Notification about change configuration
     *
     * @param changed map with changes
     */
    @Override
    public void configurationChanged(Map<String, ConfiguredVariableItem> changed) {

    }

    // inner classes
    private static class Event {
        // marker of end of working
        public static Event NULL = new Event(null,null,null);
        // event's parameters
        private final HealthItemPK module;
        private final String actionId;
        private final Object [] arguments;

        public Event(HealthItemPK module, String actionId, Object[] arguments) {
            this.module = module;
            this.actionId = actionId;
            this.arguments = arguments;
        }
    }
    private class LogDevice implements ModuleOutput.Device {
        private final HealthItemPK module;

        public LogDevice(HealthItemPK module) {
            this.module = module;
        }

        /**
         * To create ModuleOutput instance and send it to appropriate storage
         *
         * @param actionId   the id of action-aggregator (may be null)
         * @param arguments the arguments of output item for payload
         */
        @Override
        public void out(String actionId, Object... arguments) {
            LOG.debug("Output from module '{}'", module);
            eventsQueue.offer(new Event(module, actionId, arguments));
        }
    }
}
