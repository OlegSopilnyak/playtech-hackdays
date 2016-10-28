package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.*;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.health.model.factory.impl.ModuleOutputDeviceFactory;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.storage.ModuleOutputStorage;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
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
    public static final String PARAMS_PACKAGE = "health.monitor.service.module.output.log";

    @Autowired
    @Qualifier("serviceRunner")
    private ExecutorService executor;
    @Autowired
    private UniqueIdGenerator idGenerator;
    @Autowired
    private TimeService timeService;
    @Autowired
    private MonitoredActionStorage actionStorage;
    // configuration of module
    private final Map<String, ConfiguredVariableItem> config = new HashMap<>();

    @Autowired
    private ModuleOutputStorage storage;

    @Autowired
    private ModuleStateNotificationService notifier;

    private final AtomicBoolean serviceWorks = new AtomicBoolean(false);

    private final BlockingQueue<Event> eventsQueue = new LinkedBlockingQueue<>();

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
        while (serviceWorks.get());
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
                event.save();
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
        return config;
    }

    /**
     * Notification about change configuration
     *
     * @param changed map with changes
     */
    @Override
    public void configurationChanged(Map<String, ConfiguredVariableItem> changed) {
        LOG.debug("External configuration changes are received '{}'", changed);
    }

    // inner classes
    private static abstract class Event {
        // marker of end of working
        public static Event NULL = new Event(null) {@Override protected void save() {}};
        // event's parameters
        protected final HealthItemPK module;

        public Event(HealthItemPK module) {this.module = module;}
        // to save the event like Bean Managed Persistence
        protected abstract void save();
    }
    // output event
    private class OutputEvent extends Event {
        // event's parameters
        private final String actionId;
        private final Object [] arguments;

        public OutputEvent(HealthItemPK module, String actionId, Object[] arguments) {
            super(module);
            this.actionId = actionId;
            this.arguments = arguments;
        }

        @Override
        protected void save() {
            final LogMessage message = (LogMessage) storage.createModuleOutput(module, LogMessage.OUTPUT_TYPE);
            message.setId(idGenerator.generate());
            message.setActionId(actionId);
            final StringBuilder msg = new StringBuilder();
            for(Object arg : arguments) msg.append(arg);
            message.setPayload(msg.toString());
            storage.saveModuleOutput(message);
        }
    }
    // monitored action changes event
    private class ActionEvent extends Event {
        private final MonitoredAction action;

        public ActionEvent(HealthItemPK module, MonitoredAction action) {
            super(module);
            this.action = action;
        }
        @Override
        protected void save() {
            actionStorage.saveActionState(module, action);
        }
    }

    private class LogDevice implements ModuleOutput.Device {
        private final HealthItemPK module;
        private MonitoredAction action;

        public LogDevice(HealthItemPK module) {
            this.module = module;
        }

        /**
         * To create ModuleOutput instance and send it to appropriate storage
         *
         * @param arguments the arguments of output item for payload
         */
        @Override
        public void out(Object... arguments) {
            LOG.debug("Output from module '{}'", module);
            eventsQueue.offer(new OutputEvent(module, action == null ? null : action.getId(), arguments));
        }

        /**
         * Associate monitored action with module's ouput
         *
         * @param action
         */
        @Override
        public void associate(MonitoredAction action) {
            this.action = action;
            action.setState(MonitoredAction.State.INIT);
            action.setStart(timeService.now());
            eventsQueue.offer(new ActionEvent(module, action.copy()));
        }

        /**
         * To start progress stage of associated action
         */
        @Override
        public void actionBegin() {
            if (action != null){
                action.setState(MonitoredAction.State.PROGRESS);
                eventsQueue.offer(new ActionEvent(module, action.copy()));
            }
        }

        /**
         * To finish associated action successfully
         */
        @Override
        public void actionEnd() {
            if (action != null) {
                action.setState(MonitoredAction.State.SUCCESS);
                action.setFinish(timeService.now());
                action.setDuration(action.getFinish().getTime() - action.getStart().getTime());
                eventsQueue.offer(new ActionEvent(module, action.copy()));
            }
        }

        /**
         * To finish associated action with errors
         */
        @Override
        public void actionFail() {
            if (action != null) {
                action.setState(MonitoredAction.State.FAIL);
                action.setFinish(timeService.now());
                action.setDuration(action.getFinish().getTime() - action.getStart().getTime());
                eventsQueue.offer(new ActionEvent(module, action.copy()));
            }
        }
    }
}
