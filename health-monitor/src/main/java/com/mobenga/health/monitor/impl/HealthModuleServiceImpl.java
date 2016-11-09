package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.transport.ModuleWrapper;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.HealthModuleService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.storage.HealthModuleStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.mobenga.health.HealthUtils.key;

/**
 * Realization of modules service
 */
public class HealthModuleServiceImpl implements HealthModuleService, MonitoredService {
    private static final Logger LOG = LoggerFactory.getLogger(HealthModuleServiceImpl.class);
    @Value("${configuration.shared.modules.map.name:'modules'}")
    private String moduleCacheName;
    @Value("${configuration.shared.modules.queue.name:'modules-store'}")
    private String moduleQueueName;
    // the distributed cache of modules
    private Map<String, ModuleWrapper> modulesCache;
    private BlockingQueue<ModuleWrapper> storeQueue;

    @Autowired
    @Qualifier("serviceRunner")
    private ExecutorService executor;
    @Autowired
    private ModuleStateNotificationService notifier;

    // the service to persist modules
    @Autowired
    private HealthModuleStorage storage;
    // service of distributed objects
    @Autowired
    private DistributedContainersService distributed;
    private final AtomicBoolean serviceWorks = new AtomicBoolean(false);
    private volatile boolean active = false;

    public void initialize(){
        if (active) return;
        LOG.info("Starting service.");
        modulesCache = distributed.map(moduleCacheName);
        if (modulesCache.isEmpty()){
            LOG.info("It seems like it first run.");
            storage.modulesList().forEach(m-> modulesCache.put(key(m), new ModuleWrapper(m)));
        }
        storeQueue = distributed.queue(moduleQueueName);
        serviceWorks.getAndSet(false);
        executor.submit(()->this.storeUpadtes());
        while(!serviceWorks.get());
        notifier.register(this);
        LOG.info("Service started.");
    }

    public void shutdown(){
        if (!active) return;
        LOG.info("Stopping service.");
        active  = false;
        while(serviceWorks.get());
        notifier.unRegister(this);
        LOG.info("Service stopped.");
    }
    /**
     * To get cached module by real module
     *
     * @param module real module of application
     * @return the wrapper of module
     */
    @Override
    public HealthItemPK getModule(HealthItemPK module) {
        ModuleWrapper wModule = modulesCache.get(key(module));
        if (wModule == null){
            wModule = new ModuleWrapper(module);
            storeQueue.offer(wModule);
            modulesCache.put(key(module), wModule);
        }
        return wModule;
    }

    /**
     * To gte cached module by module's key
     *
     * @param moduleKey module's key
     * @return the wrapper of module
     */
    @Override
    public HealthItemPK getModule(String moduleKey) {
        return modulesCache.get(moduleKey);
    }

    /**
     * To get the list of all registered modules
     *
     * @return list of wrappers of modules
     */
    @Override
    public List<HealthItemPK> getModules() {
        return modulesCache.values().stream().collect(Collectors.toList());
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
        return active;
    }

    /**
     * The handle to restart monitored service
     */
    @Override
    public void restart() {
        shutdown();
        initialize();
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
        return "healthModulesManagement";
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
        return "Module to make distributed access to modules set.";
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
        LOG.debug("Received changes ", changed);
    }

    @Override
    public String toString() {
        return "-HealthModuleService-";
    }

    // private methods
    private void storeUpadtes(){
        serviceWorks.getAndSet(active = true);
        try {
            while (active) {
                final ModuleWrapper module = storeQueue.poll(100, TimeUnit.MILLISECONDS);
                storage.save(module);
            }
        } catch (InterruptedException e) {
            LOG.error("Poll was interrupted ", e);
        } catch (Throwable e) {
            LOG.error("Unexpected ", e);
        } finally {
            serviceWorks.getAndSet(active = false);
        }
    }

}
