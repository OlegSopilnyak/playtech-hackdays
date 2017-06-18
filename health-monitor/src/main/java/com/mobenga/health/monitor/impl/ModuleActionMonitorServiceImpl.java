package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.model.transport.ModuleWrapper;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.ModuleMonitoringService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.mobenga.health.HealthUtils.key;
import com.mobenga.health.model.ModulePK;
import java.util.function.Consumer;

/**
 * realization of core monitoring service
 *
 * @see ModuleMonitoringService
 */
public class ModuleActionMonitorServiceImpl extends AbstractRunningService implements ModuleMonitoringService {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleActionMonitorServiceImpl.class);

    @Autowired
    private MonitoredActionStorage storage;

    @Autowired
    private ModuleStateNotificationService notifier;

    @Autowired
    private DistributedContainersService distributed;

    private MonitoredAction templateAction;

    @Value("${configuration.shared.actions.queue.name:'actions-storage-queue'}")
    private String sharedQueueName;
    private BlockingQueue<StoreActionWrapper> distributedStorageQueue;

    private final Map<String, ConfiguredVariableItem> config = new HashMap<>();

    private String ignoreModules = IGRNORE_MODULES.get(String.class);

    public ModuleActionMonitorServiceImpl() {
        config.put(IGNORE_MODULES_FULL_NAME, IGRNORE_MODULES);
    }

    @Override
    protected Logger getLogger() {return LOG;}

    /**
     * To get the value of Module's PK
     *
     * @return value of PK (not null)
     */
    @Override
    public ModulePK getModulePK() {return this;}

    public void initialize() {
        super.start();
    }

    @Override
    protected void beforeStart() {
        distributedStorageQueue = distributed.queue(sharedQueueName);
        templateAction = storage.createMonitoredAction();
        notifier.register(this);
    }

    @Override
    protected void afterStart() {}

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    protected void beforeStop() {
        templateAction = null;
    }

    @Override
    protected void afterStop() {
        notifier.unRegister(this);
    }

    public String getIgnoreModules() {
        return ignoreModules;
    }

    public void setIgnoreModules(String ignoreModules) {
        config.get(IGNORE_MODULES_FULL_NAME).set(this.ignoreModules = ignoreModules);
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
    public void actionMonitoring(ModulePK module, MonitoredAction action) {
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

    @Override
    public String toString() {
        return "-ModuleActionMonitorService-";
    }

    @Override
    protected void serviceLoopIteration() {
        try {
            final StoreActionWrapper wrappedAction = distributedStorageQueue.poll(100, TimeUnit.MILLISECONDS);
            if (wrappedAction != null) {
                LOG.debug("Saving MonitoredAction '{}' for '{}'", new Object[]{wrappedAction.action, wrappedAction.module});
                storage.saveActionState(wrappedAction.module, wrappedAction.action);
            }
        } catch (InterruptedException ex) {
            LOG.error("Distributed queue threw", ex);
            super.active = false;
        }
    }

    // private methods
    private boolean moduleIsIgnored(ModulePK module) {
        return moduleIsIgnored(key(module));
    }

    private boolean moduleIsIgnored(String moduleKey) {
        // check the direct ignorance
        if (ignoreModules.contains(moduleKey)) {
            return true;
        }
        // check the groups
        final StringTokenizer st = new StringTokenizer(ignoreModules, " ,");
        while (st.hasMoreTokens()) {
            String ignored = st.nextToken();
            if (ignored.endsWith("*")) {
                // cut off the start-symbol
                ignored = ignored.substring(0, ignored.length() - 2);
            }
            if (moduleKey.startsWith(ignored)) {
                return true;
            }
        }
        return false;
    }

    // private classes
    private static class StoreActionWrapper implements Serializable {

        private static final long serialVersionUID = -6242682305211719324L;

        final ModuleWrapper module;
        final MonitoredAction action;

        public StoreActionWrapper(ModulePK module, MonitoredAction action) {
            this.module = new ModuleWrapper(module);
            this.action = action;
        }
    }
}
