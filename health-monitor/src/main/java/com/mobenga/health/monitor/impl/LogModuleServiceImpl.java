package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.MonitoredAction;
import com.mobenga.health.model.business.out.ModuleOutputDevice;
import com.mobenga.health.model.business.out.ModuleOutputDeviceFarm;
import com.mobenga.health.model.business.out.log.ModuleLoggerDevice;
import com.mobenga.health.model.business.out.log.ModuleLoggerDeviceFactory;
import com.mobenga.health.model.business.out.log.ModuleLoggerMessage;
import com.mobenga.health.model.transport.ModuleKeyDto;
import com.mobenga.health.model.transport.MonitoredActionDto;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.TimeService;
import com.mobenga.health.monitor.UniqueIdGenerator;
import com.mobenga.health.storage.ModuleOutputStorage;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mobenga.health.HealthUtils.key;

/**
 * The service handles module's output type "log"
 */
public class LogModuleServiceImpl extends AbstractRunningService implements ModuleLoggerDeviceFactory, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(LogModuleServiceImpl.class);

    // the template for monitored actions
    private static final MonitoredActionDto actionTemplate = new MonitoredActionDto();


    @Autowired
    private UniqueIdGenerator idGenerator;

    @Autowired
    private TimeService timeService;
    @Autowired
    private MonitoredActionStorage actionStorage;

    @Autowired
    private ModuleOutputStorage outputStorage;

    @Autowired
    private ModuleStateNotificationService notifier;

    @Autowired
    private DistributedContainersService distributed;

    @Value("${configuration.shared.output.log.queue.name:'log-output-storage-queue'}")
    private String sharedQueueName;
    private BlockingQueue<Event> distributedStorageQueue;
    // queue for messages of inactive service
    private final BlockingQueue<Event> offlineQueue = new LinkedBlockingQueue<>();
    // the set of modules to ignore output
    private String ignoreModules = IGNORE_MODULES.get(String.class);
    private Set<String> ignored = new LinkedHashSet<>();
    // configuration of module
    private final Map<String, ConfiguredVariableItem> config = new LinkedHashMap<>();


    public LogModuleServiceImpl() {
        config.put(IGNORE_MODULES_FULL_NAME, IGNORE_MODULES);
        buildIgnoredModules();
        // register the device
        ModuleOutputDeviceFarm.registerDeviceFactory(this);
    }

    /**
     * Return a delay between run iterations
     *
     * @return the value
     */
    @Override
    protected long scanDelayMillis() {
        return 200L;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        startService();
    }

    public void startService() {
        super.start();
    }

    @Override
    protected void beforeStart() {
        distributedStorageQueue = distributed.queue(sharedQueueName);
    }

    @Override
    protected void afterStart() {
        offlineQueue.forEach(e -> distributedStorageQueue.offer(e));
        offlineQueue.clear();
        notifier.register(this);
    }

    @Override
    protected void serviceLoopIteration() throws InterruptedException {
        if (!isActive()) {
            return;
        }

        Event event = null;
        while (isActive() && (event = distributedStorageQueue.poll(100, TimeUnit.MILLISECONDS)) != null) {
            event.save(this);
        }
    }

    @Override
    protected void serviceLoopException(Throwable t) {
        LOG.error("Something went wrong", t);
    }


    public void stopService() {
        super.shutdown();
    }

    @Override
    protected void beforeStop() {
        distributedStorageQueue = null;
    }

    @Override
    protected void afterStop() {
        offlineQueue.clear();
        notifier.unRegister(this);
    }

    /**
     * To create the Device for module's output
     *
     * @param module module-source of output
     * @return the instance
     */
    @Override
    public ModuleOutputDevice create(ModuleKey module) {
        return new ModuleLogger(module);
    }

    /**
     * returns supported type of ModuleOutputMessage
     *
     * @return the value
     */
    @Override
    public String getType() {
        return ModuleLoggerMessage.LOG_OUTPUT_TYPE;
    }

    /**
     * To check is module ignored for saving
     *
     * @param module module to check
     * @return true if ignored
     */
    @Override
    public boolean isModuleIgnored(ModuleKey module) {
        return moduleIsIgnored(key(module));
    }

    public String getIgnoreModules() {
        return ignoreModules;
    }

    public void setIgnoreModules(String ignoreModules) {
        config.get(IGNORE_MODULES_FULL_NAME).set(this.ignoreModules = ignoreModules);
        buildIgnoredModules();
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
        // updating ignore-modules parameter
        updateParameter(changed, IGNORE_MODULES_FULL_NAME, i -> setIgnoreModules(i.get(String.class)));

        // save new items of configuration
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

    private boolean moduleIsIgnored(final String moduleKey) {
        final Optional<String> ignore = ignored.stream()
                .filter(pattern -> pattern.equals(moduleKey))
                .filter(pattern -> pattern.endsWith("*"))
                .map(pattern -> pattern.substring(0, pattern.length() - 2))
                .filter(pattern -> moduleKey.startsWith(pattern))
                .findFirst();
        return ignore.isPresent();
    }

    private void buildIgnoredModules() {
        ignored = Collections.list(new StringTokenizer(ignoreModules, " ,"))
                .stream().map(token -> (String) token).collect(Collectors.toSet());
    }

    void setNotifier(ModuleStateNotificationServiceImpl notifier) {
        this.notifier = notifier;
    }


    // inner classes
    private static abstract class Event implements Serializable {

        private static final long serialVersionUID = 6538641586708962438L;
        // event's parameters
        protected final ModuleKeyDto module;

        public Event(ModuleKey module) {
            this.module = new ModuleKeyDto(module);
        }

        /**
         * To save the values of event
         *
         * @param service log-service instance
         */
        protected abstract void save(LogModuleServiceImpl service);
    }

    // output event
    private static class OutputEvent extends Event {

        private static final long serialVersionUID = 212382839050306882L;
        // event's parameters
        private final String actionId;
        private final Object[] arguments;

        public OutputEvent(ModuleKey module, String actionId, Object[] arguments) {
            super(module);
            this.actionId = actionId;
            this.arguments = arguments;
        }

        // to save the event like Bean Managed Persistence
        @Override
        protected void save(final LogModuleServiceImpl service) {
            if (service.moduleIsIgnored(key(module))) {
                LOG.warn("The module '{}' is ignored for save.", key(module));
                return;
            }
            final ModuleLoggerMessage message =
                    (ModuleLoggerMessage) service.outputStorage.createModuleOutput(module, ModuleLoggerMessage.LOG_OUTPUT_TYPE);

            if (message == null) {
                // output message couldn't created
                return;
            }
            // prepare and save created output message
            message.setId(service.idGenerator.generate());
            message.setActionId(actionId);
            message.setWhenOccured(service.timeService.now());
            final StringBuilder msg = new StringBuilder();
            for (Object arg : arguments) msg.append(arg);
            message.setPayload(msg.toString());
            // persistence the output message
            service.outputStorage.saveModuleOutput(message);
        }
    }

    // monitored newAction changes event
    private static class ActionEvent extends Event {

        private static final long serialVersionUID = 4277689698575052493L;
        private final MonitoredAction action;

        public ActionEvent(ModuleKey module, MonitoredAction action) {
            super(module);
            this.action = action;
        }

        // to save the event like Bean Managed Persistence
        @Override
        protected void save(final LogModuleServiceImpl service) {
            if (service.moduleIsIgnored(key(module))) {
                LOG.debug("The module '{}' is ignored for save.", key(module));
                return;
            }
            // save updated monitored action
            service.actionStorage.saveActionState(module, action);
        }
    }

    // device to output log information
    private class ModuleLogger extends ModuleLoggerDevice {

        public ModuleLogger(ModuleKey module) {
            super(module);
        }

        @Override
        public boolean isActive() {
            return LogModuleServiceImpl.this.isActive();
        }

        @Override
        protected void asyncOutput(ModuleKey module, MonitoredAction action, Object... arguments) {
            LOG.debug("Output from module '{}'", module);
            try {
                distributedQueue().put(new OutputEvent(module, action == null ? null : action.getId(), arguments));
            } catch (InterruptedException ex) {
                LOG.error("Cannot transfer arguments", ex);
            }
        }

        @Override
        protected MonitoredAction createMonitoredAction() {
            return actionTemplate.copy();
        }

        @Override
        protected void asyncInitMonitoredAction(ModuleKey module, MonitoredAction action) {
            ((MonitoredActionDto) action).setState(MonitoredAction.State.INIT);
            ((MonitoredActionDto) action).setStart(timeService.now());
            try {
                distributedQueue().put(new ActionEvent(new ModuleKeyDto(module), action.copy()));
            } catch (InterruptedException ex) {
                LOG.error("Cannot transfer new action", ex);
            }
        }

        @Override
        protected void asyncUpdateMonitoredAction(ModuleKey module, MonitoredAction action, MonitoredAction.State state) {
            ((MonitoredActionDto) action).setState(state);
            switch (state) {
                case PROGRESS:
                    ((MonitoredActionDto) action).setStart(timeService.now());
                    break;
                case SUCCESS:
                case FAIL:
                    ((MonitoredActionDto) action).setFinish(timeService.now());
                    ((MonitoredActionDto) action).setDuration(action.getFinish().getTime() - action.getStart().getTime());
                    break;
            }
            try {
                distributedQueue().put(new ActionEvent(new ModuleKeyDto(module), action.copy()));
            } catch (InterruptedException ex) {
                LOG.error("Cannot transfer updates of action", ex);
            }
        }
    }
}
