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
import com.mobenga.health.monitor.ModuleLoggerDeviceFactory;

/**
 * The service handles module's output type "log"
 */
public class LogModuleServiceImpl implements ModuleLoggerDeviceFactory, ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(LogModuleServiceImpl.class);

    private volatile MonitoredAction actionTemplate = null;
            
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
    private final BlockingQueue<Event> offlineQueue = new LinkedBlockingQueue<>();

    private String ignoreModules = IGNORE_MODULES.get(String.class);

    public LogModuleServiceImpl() {
        config.put(IGNORE_MODULES_FULL_NAME, IGNORE_MODULES);
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
     * @param module module-source of output
     * @return the instance
     */
    @Override
    public ModuleOutput.Device create(ModulePK module) {
        return new ModuleLogger(module);
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
    public boolean isModuleIgnored(ModulePK module) {
        return moduleIsIgnored(key(module));
    }

    public String getIgnoreModules() {
        return ignoreModules;
    }

    public void setIgnoreModules(String ignoreModules) {
        config.get(IGNORE_MODULES_FULL_NAME).set(this.ignoreModules = ignoreModules);
    }

    /**
     * To get the value of Module's PK
     *
     * @return value of PK (not null)
     */
    @Override
    public ModulePK getModulePK() {
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
        final ConfiguredVariableItem item = changed.get(IGNORE_MODULES_FULL_NAME);
        if (item != null){
            setIgnoreModules(item.get(String.class));
        }
        config.putAll(changed);
    }

    @Override
    public String toString() {
        return "-LoggerModuleService-";
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

        private static final long serialVersionUID = 6538641586708962438L;
        // event's parameters
        protected final ModuleWrapper module;

        public Event(ModulePK module) {this.module = new ModuleWrapper(module);}
        // to save the event like Bean Managed Persistence

        /**
         * To save the values of event
         * @param service log-service instance
         */
        protected abstract void save(LogModuleServiceImpl service);
    }
    // output event
    private static class OutputEvent extends Event {

        private static final long serialVersionUID = 212382839050306882L;
        // event's parameters
        private final String actionId;
        private final Object [] arguments;

        public OutputEvent(ModulePK module, String actionId, Object[] arguments) {
            super(module);
            this.actionId = actionId;
            this.arguments = arguments;
        }

        @Override
        protected void save(final LogModuleServiceImpl service) {
            final String moduleKey = key(module);
            if (service.moduleIsIgnored(moduleKey)){
                LOG.warn("The module '{}' is ignored for save.", moduleKey);
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
    // monitored newAction changes event
    private static class ActionEvent extends Event {

        private static final long serialVersionUID = 4277689698575052493L;
        private final MonitoredAction action;

        public ActionEvent(ModulePK module, MonitoredAction action) {
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

    private class ModuleLogger extends AbstractLogger{

        public ModuleLogger(ModulePK module) {
            super(module);
        }

        @Override
        protected void asyncOutput(ModulePK module, MonitoredAction action, Object... arguments) {
            LOG.debug("Output from module '{}'", module);
            distributedQueue().offer(new OutputEvent(module, action == null ? null : action.getId(), arguments));
        }

        @Override
        protected MonitoredAction createMonitoredAction() {
            if (actionTemplate != null) {
                return actionTemplate.copy();
            }
            synchronized (ModuleLogger.class) {
                if (actionTemplate == null) {
                    actionTemplate = actionStorage.createMonitoredAction();
                }
            }
            return actionTemplate.copy();
        }

        @Override
        protected void asyncInitMonitoredAction(ModulePK module, MonitoredAction action) {
            action.setState(MonitoredAction.State.INIT);
            action.setStart(timeService.now());
            distributedQueue().offer(new ActionEvent(new ModuleWrapper(module), action.copy()));
        }

        @Override
        protected void asyncUpdateMonitoredAction(ModulePK module, MonitoredAction action, MonitoredAction.State state) {
            action.setState(state);
            switch (state) {
                case PROGRESS:
                    action.setStart(timeService.now());
                    break;
                case SUCCESS:
                case FAIL:
                    action.setFinish(timeService.now());
                    action.setDuration(action.getFinish().getTime() - action.getStart().getTime());
                    break;
            }
            distributedQueue().offer(new ActionEvent(new ModuleWrapper(module), action.copy()));
        }
    }
    private class LogDeviceOld implements ModuleOutput.Device {
        private final ModulePK module;
        private MonitoredAction action;

        public LogDeviceOld(ModulePK module) {
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
         * Associate monitored newAction with module's output
         *
         * @param newAction
         */
        @Override
        public void associate(MonitoredAction newAction) {
            if ((this.action = newAction) != null) {
                newAction.setState(MonitoredAction.State.INIT);
                newAction.setStart(timeService.now());
                distributedQueue().offer(new ActionEvent(new ModuleWrapper(module), newAction.copy()));
            }
        }

        /**
         * To create and associate newAction
         *
         * @param actionDescription description of newAction
         */
        @Override
        public void associate(String actionDescription) {
            final MonitoredAction newAction = actionStorage.createMonitoredAction();
            newAction.setDescription(actionDescription);
            newAction.setState(MonitoredAction.State.INIT);
            newAction.setStart(timeService.now());
            actionStorage.saveActionState(module, newAction);
            this.action = newAction;
        }

        /**
         * To get associated newAction
         *
         * @return instance or null if no association
         */
        @Override
        public MonitoredAction getAssociated() {
            return action;
        }

        /**
         * To start progress stage of associated newAction
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
         * To finish associated newAction successfully
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
         * To finish associated newAction with errors
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
