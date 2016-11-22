package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.*;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.health.model.factory.impl.ModuleOutputDeviceFactory;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.model.transport.ModuleWrapper;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.storage.ModuleOutputStorage;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mobenga.health.HealthUtils.key;

/**
 * The service handles module's output type "log"
 */
public class LogModuleServiceImpl implements ModuleOutput.DeviceFactory, MonitoredService, ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(LogModuleServiceImpl.class);
    public static final String PARAMS_PACKAGE = "health.monitor.service.module.output.log";
    public static final String IGNORE_MODULES = "ignoreModules";

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

    @Autowired
    private DistributedContainersService distributed;
    private final AtomicBoolean serviceWorks = new AtomicBoolean(false);
    private volatile boolean active = false;

    @Value("${configuration.shared.output.log.queue.name:'log-output-storage-queue'}")
    private String sharedQueueName;
    private BlockingQueue<Event> distributedStorageQueue;
    private BlockingQueue<Event> offlineQueue = new LinkedBlockingQueue<>();

    private String ignoreModules;

    public LogModuleServiceImpl() {
        final LocalConfiguredVariableItem im =
                new LocalConfiguredVariableItem(IGNORE_MODULES, "The set of modules to ignore logging for.", "none");
        config.put(PARAMS_PACKAGE+"."+IGNORE_MODULES, im);
        ignoreModules = im.get(String.class);
        // register the device
        ModuleOutputDeviceFactory.registerDeviceFactory(this);
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        startService();
    }

    public void startService(){
        if (active) return;
        distributedStorageQueue = distributed.queue(sharedQueueName);
        LOG.info("Starting service storage='{}'", storage);
        executor.submit(()->processEvents());
        while (!serviceWorks.get());
        // register the module
        notifier.register(this);
    }

    public void stopService(){
        if (!active) return;
        LOG.info("Stopping service storage='{}'", storage);
        active  = false;
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

    /**
     * To check is module ignored for saving
     *
     * @param module module to check
     * @return true if ignored
     */
    @Override
    public boolean isModuleIgnored(HealthItemPK module) {
        return moduleIsIgnored(key(module));
    }

    public String getIgnoreModules() {
        return ignoreModules;
    }

    public void setIgnoreModules(String ignoreModules) {
        config.get(PARAMS_PACKAGE+"."+IGNORE_MODULES).set(this.ignoreModules = ignoreModules);
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
        stopService();
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
        if (changed.isEmpty()){
            return;
        }
        final ConfiguredVariableItem item = changed.get(PARAMS_PACKAGE+"."+IGNORE_MODULES);
        if (item != null){
            setIgnoreModules(item.get(String.class));
        }
        config.putAll(changed);
    }

    @Override
    public String toString() {
        return "-LogModuleService-";
    }

    // private methods
    private BlockingQueue<Event> distributedQueue() {
        return distributedStorageQueue == null ? offlineQueue : distributedStorageQueue;
    }

    private void processEvents(){
        offlineQueue.forEach( e -> distributedStorageQueue.offer(e));
        offlineQueue.clear();
        serviceWorks.getAndSet(active = true);
        try{
            while (active){
                final Event event = distributedStorageQueue.poll(100, TimeUnit.MILLISECONDS);
                if (event != null){
                    event.save(this);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Events queue iterrupted", e);
        } catch (Throwable t) {
            LOG.error("Caught unexpected exception", t);
        } finally {
            serviceWorks.getAndSet(active = false);
            LOG.info("Service stopped.");
        }
    }
    private boolean moduleIsIgnored(String moduleKey){
        // check the direct ignorance
        if (ignoreModules.contains(moduleKey)) {
            return true;
        }
        // check the groups
        final StringTokenizer st = new StringTokenizer(ignoreModules, " ,");
        while(st.hasMoreTokens()){
            String ignored = st.nextToken();
            if (ignored.endsWith("*")){
                // cut off the start-symbol
                ignored = ignored.substring(0, ignored.length() - 2);
            }
            if (moduleKey.startsWith(ignored)){
                return true;
            }
        }
        return false;
    }


    // inner classes
    private static abstract class Event implements Serializable {
        // event's parameters
        protected final ModuleWrapper module;

        public Event(HealthItemPK module) {this.module = new ModuleWrapper(module);}
        // to save the event like Bean Managed Persistence

        /**
         * To save the values of event
         * @param service log-service instance
         */
        protected abstract void save(LogModuleServiceImpl service);
    }
    // output event
    private static class OutputEvent extends Event {
        // event's parameters
        private final String actionId;
        private final Object [] arguments;

        public OutputEvent(HealthItemPK module, String actionId, Object[] arguments) {
            super(module);
            this.actionId = actionId;
            this.arguments = arguments;
        }

        @Override
        protected void save(final LogModuleServiceImpl service) {
            final String moduleKey = key(module);
            if (service.moduleIsIgnored(moduleKey)){
//                LOG.warn("The module '{}' is ignored for save.", moduleKey);
                return;
            }
            final LogMessage message = (LogMessage) service.storage.createModuleOutput(module, LogMessage.OUTPUT_TYPE);
            if (message != null) {
                message.setId(service.idGenerator.generate());
                message.setActionId(actionId);
                message.setWhenOccured(service.timeService.now());
                final StringBuilder msg = new StringBuilder();
                for (Object arg : arguments) msg.append(arg);
                message.setPayload(msg.toString());
                service.storage.saveModuleOutput(message);
            }
        }
    }
    // monitored action changes event
    private static class ActionEvent extends Event {
        private final MonitoredAction action;

        public ActionEvent(HealthItemPK module, MonitoredAction action) {
            super(module);
            this.action = action;
        }
        @Override
        protected void save(final LogModuleServiceImpl service) {
            final String moduleKey = key(module);
            if (service.moduleIsIgnored(moduleKey)){
//                LOG.warn("The module '{}' is ignored for save.", moduleKey);
                return;
            }
            service.actionStorage.saveActionState(module, action);
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
            distributedQueue().offer(new OutputEvent(module, action == null ? null : action.getId(), arguments));
        }

        /**
         * Associate monitored action with module's ouput
         *
         * @param action
         */
        @Override
        public void associate(MonitoredAction action) {
            if ((this.action = action) != null) {
                action.setState(MonitoredAction.State.INIT);
                action.setStart(timeService.now());
                distributedQueue().offer(new ActionEvent(new ModuleWrapper(module), action.copy()));
            }
        }

        /**
         * To create and associate action
         *
         * @param actionDescription description of action
         */
        @Override
        public void associate(String actionDescription) {
            final MonitoredAction action = actionStorage.createMonitoredAction();
            action.setDescription(actionDescription);
            action.setState(MonitoredAction.State.INIT);
            action.setStart(timeService.now());
            actionStorage.saveActionState(module, action);
            this.action = action;
        }

        /**
         * To get associated action
         *
         * @return instance or null if no association
         */
        @Override
        public MonitoredAction getAssociated() {
            return action;
        }

        /**
         * To start progress stage of associated action
         */
        @Override
        public void actionBegin() {
            if (action != null){
                action.setStart(timeService.now());
                action.setState(MonitoredAction.State.PROGRESS);
                distributedQueue().offer(new ActionEvent(module, action.copy()));
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
                distributedQueue().offer(new ActionEvent(module, action.copy()));
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
                distributedQueue().offer(new ActionEvent(module, action.copy()));
            }
        }
    }
}
