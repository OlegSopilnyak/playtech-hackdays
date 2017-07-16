package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.MonitoredAction;
import com.mobenga.health.model.transport.ModuleKeyDto;
import com.mobenga.health.model.transport.MonitoredActionDto;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.ModuleMonitoringService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mobenga.health.HealthUtils.key;

/**
 * realization of core monitoring service
 *
 * @see ModuleMonitoringService
 */
public class ModuleActionMonitorServiceImpl extends AbstractRunningService implements ModuleMonitoringService {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleActionMonitorServiceImpl.class);

    private final MonitoredActionDto templateAction = new MonitoredActionDto();

    @Autowired
    private MonitoredActionStorage storage;

    @Autowired
    private ModuleStateNotificationService notifier;

    @Autowired
    private DistributedContainersService distributed;

    @Value("${configuration.shared.actions.queue.name:'actions-storage-queue'}")
    private String sharedQueueName;
    private BlockingQueue<StoreActionWrapper> distributedStorageQueue;

    private final Map<String, ConfiguredVariableItem> config = new HashMap<>();

    private String ignoreModules = IGRNORE_MODULES.get(String.class);
    private Set<String> ignored = new LinkedHashSet<>();

    public ModuleActionMonitorServiceImpl() {
        config.put(IGNORE_MODULES_FULL_NAME, IGRNORE_MODULES);
        buildIgnoredModules();
    }

    @Override
    protected Logger getLogger() {return LOG;}

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
    public String toString() {
        return "-ModuleActionMonitorService-";
    }


    public void initialize() {
        super.start();
    }

    @Override
    protected void beforeStart() {
        distributedStorageQueue = distributed.queue(sharedQueueName);
        notifier.register(this);
    }

    @Override
    protected void afterStart() {}

    @Override
    protected void serviceLoopIteration() throws InterruptedException {
        if (!isActive()) return;

        StoreActionWrapper wrappedAction = null;
        while (isActive() && (wrappedAction = distributedStorageQueue.poll(100, TimeUnit.MILLISECONDS)) != null) {
            LOG.debug("Saving MonitoredAction '{}' for '{}'", new Object[]{wrappedAction.action, wrappedAction.module});
            storage.saveActionState(wrappedAction.module, wrappedAction.action);
        }
    }

    @Override
    protected void serviceLoopException(Throwable t) {
        LOG.error("Something went wrong", t);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    protected void beforeStop() {
        distributedStorageQueue = null;
    }

    @Override
    protected void afterStop() {
        notifier.unRegister(this);
    }

    public void setIgnoreModules(String ignoreModules) {
        config.get(IGNORE_MODULES_FULL_NAME).set(this.ignoreModules = ignoreModules);
        buildIgnoredModules();
    }

    /**
     * To create the instance of MonitoredAction.class
     *
     * @return the instance doesn't attached to database
     */
    @Override
    public MonitoredAction createMonitoredAction() {
        LOG.debug("Creating MonitoredAction instance");
        return templateAction.copy();
    }

    /**
     * To monitoring the action proceed in the system
     *
     * @param module the owner of action
     * @param action monitored action bean
     */
    @Override
    public void actionMonitoring(ModuleKey module, MonitoredAction action) {
        if (!isActive()) return;
        
        if (!moduleIsIgnored(module)) {
            distributedStorageQueue.offer(new StoreActionWrapper(module, action));
        }
    }

    /**
     * The handle to restart monitored service
     */
    @Override
    public void restart() {
        LOG.info("Restarting...");
        shutdown();
        initialize();
    }

    /**
     * To get current configuration of module
     *
     * @return the map
     */
    @Override
    public Map<String, ConfiguredVariableItem> getConfiguration() {return config;}

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
    // private methods
    private boolean moduleIsIgnored(ModuleKey module) {
        return moduleIsIgnored(key(module));
    }

    private boolean moduleIsIgnored(final String moduleKey) {
        final Optional<String> ignore = ignored.stream()
                .filter(pattern -> pattern.equals(moduleKey))
                .filter(pattern -> pattern.endsWith("*"))
                .map(pattern -> pattern.substring(0, pattern.length() - 2))
                .filter(prefix -> moduleKey.startsWith(prefix))
                .findFirst();
        return ignore.isPresent();
    }

    private void buildIgnoredModules() {
        ignored = Collections.list(new StringTokenizer(ignoreModules, " ,"))
                .stream().map(token -> (String) token).collect(Collectors.toSet());
    }

    // private classes
    private static class StoreActionWrapper implements Serializable {

        private static final long serialVersionUID = -6242682305211719324L;

        final ModuleKeyDto module;
        final MonitoredAction action;

        StoreActionWrapper(ModuleKey module, MonitoredAction action) {
            this.module = new ModuleKeyDto(module);
            this.action = action;
        }
    }
}
