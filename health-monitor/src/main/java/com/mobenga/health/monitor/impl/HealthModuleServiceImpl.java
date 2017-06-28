package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.transport.ModuleKeyDto;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.HealthModuleService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.storage.HealthModuleStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mobenga.health.HealthUtils.key;
import com.mobenga.health.model.ModulePK;

/**
 * Realization of modules service
 */
public class HealthModuleServiceImpl extends AbstractRunningService implements HealthModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(HealthModuleServiceImpl.class);

    // names of distributed containers
    @Value("${configuration.shared.modules.map.name:'modules'}")
    private String moduleCacheName;
    @Value("${configuration.shared.modules.queue.name:'modules-store'}")
    private String moduleQueueName;

    @Autowired
    private ModuleStateNotificationService notifier;

    // the service to persist modules
    @Autowired
    private HealthModuleStorage storage;
    // service of distributed objects
    @Autowired
    private DistributedContainersService distributed;

    // distributed cached vontainers of module
    private Map<String, ModuleKeyDto> modulesCache;
    private BlockingQueue<ModuleKeyDto> storeQueue;

    /**
     * To get the value of Module's PK
     *
     * @return value of PK (not null)
     */
    @Override
    public ModulePK getModulePK() {return this;}
    /**
     * Represent module as a string
     * @return string
     */
    @Override
    public String toString() {return "-HealthModuleService-";}
    
    public void initialize() {super.start();}

    @Override
    public void shutdown() {super.shutdown();}

    /**
     * To get cached module by real module
     *
     * @param module real module of application
     * @return the wrapper of module
     */
    @Override
    public ModulePK getModule(final ModulePK module) {
        return modulesCache.computeIfAbsent(key(module), (String mk) -> {
            final ModuleKeyDto wrapper;
            storeQueue.offer(wrapper = new ModuleKeyDto(module));
            return wrapper;
        });
    }

    /**
     * To get cached module by module's key
     *
     * @param moduleId module's key
     * @return the wrapper of module
     */
    @Override
    public ModulePK getModule(String moduleId) {
        return modulesCache.get(moduleId);
    }

    /**
     * To get the list of all registered modules
     *
     * @return list of wrappers of modules
     */
    @Override
    public List<ModulePK> getModules() {
        return modulesCache.values().stream().collect(Collectors.toList());
    }


    /**
     * The handle to restart monitored service
     */
    @Override
    public void restart() {this.shutdown(); this.initialize();}

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

    // redefined protected methods
    @Override
    protected void beforeStart() {
        modulesCache = distributed.map(moduleCacheName);
        if (modulesCache.isEmpty()) {
            LOG.info("It seems like it first run.");
            storage.modulesList().forEach(m -> modulesCache.put(key(m), new ModuleKeyDto(m)));
        }
        storeQueue = distributed.queue(moduleQueueName);
    }
    @Override
    protected void afterStart() {notifier.register(this);}

    @Override
    protected void beforeStop() {}
    @Override
    protected void afterStop() {notifier.unRegister(this);}

    @Override
    protected Logger getLogger() {return LOG;}

    @Override
    protected void serviceLoopIteration() {
        try {
            final ModuleKeyDto module = storeQueue.poll(100, TimeUnit.MILLISECONDS);
            if (module != null) {
                storage.save(module);
            }
        } catch (InterruptedException e) {
            LOG.error("Poll was interrupted ", e);
        }
    }

    // private methods
}
