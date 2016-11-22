package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.model.transport.ModuleWrapper;
import com.mobenga.health.monitor.*;
import com.mobenga.health.storage.ConfigurationStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.mobenga.health.HealthUtils.key;

/**
 * Realization of module configuration
 *
 * @see ModuleConfigurationService
 */
public class ModuleConfigurationServiceImpl implements ModuleConfigurationService, MonitoredService{

    private static final Logger LOG = LoggerFactory.getLogger(ModuleConfigurationServiceImpl.class);

    private Map<String, Map<String, ConfiguredVariableItem>> sharedCache;
    private BlockingQueue<ModuleConfigurationServiceImpl.StoreEvent> sharedQueue;

    @Value("${configuration.shared.config.map.name:'modules-configuration-map'}")
    private String sharedMapName;
    @Value("${configuration.shared.config.queue.name:'modules-configuration-queue'}")
    private String sharedQueueName;

    @Autowired
    private DistributedContainersService distributed;

    @Autowired
    private ConfigurationStorage storage;

    @Autowired
    private HealthModuleService modules;

    @Autowired
    private ModuleStateNotificationService notifier;

    @Autowired
    @Qualifier("serviceRunner")
    private ExecutorService executor;
    private final AtomicBoolean serviceRuns = new AtomicBoolean(false);
    private volatile boolean active;

    private final Map<String, ConfiguredVariableItem> config = new HashMap<>();

    public void initialize(){
        if (active) return;
        final Map<String, Map<String, ConfiguredVariableItem>> sharedMap = distributed.map(sharedMapName);
        if (sharedMap.isEmpty()) {
            LOG.info("It seems node is alone. Loading stored configurations.");
            modules.getModules().stream().map(m -> key(m))
                    .forEach(module -> sharedMap.putIfAbsent(module, storage.getConfiguration(module)));
        }
        sharedCache = sharedMap;
        sharedQueue = distributed.queue(sharedQueueName);
        executor.submit(()->processStoreEventsQueue());
        while (!serviceRuns.get());
        notifier.register(this);
    }

    public void shutdown(){
        if (!active) return;
        active = false;
        while (serviceRuns.get());
        notifier.unRegister(this);
    }
    /**
     * To get the configuration of module
     *
     * @param module the consumer of configurations
     * @param groupName   the dot-delimited name of group (empty is root)
     * @return map of full-qualified configured variables
     */
    @Override
    public Map<String, ConfiguredVariableItem> getConfigurationGroup(HealthItemPK module, String groupName) {
        final String normalizedGroup = normal(groupName);
        final String moduleKey = key(modules.getModule(module));
        LOG.debug("Getting configuration for '{}' from group '{}'", new Object[]{moduleKey, normalizedGroup});
        return sharedCache.computeIfAbsent(moduleKey, (mk) -> new LinkedHashMap<>()).entrySet().stream()
                .filter(p -> p.getKey().startsWith(normalizedGroup))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    }

    /**
     * To get updated configured variables
     *
     * @param module   the consumer of configuration
     * @param currentConfiguration current state of configuration
     * @return updated variables (emptyMap if none)
     */
    @Override
    public Map<String, ConfiguredVariableItem> getUpdatedVariables(HealthItemPK module, Map<String, ConfiguredVariableItem> currentConfiguration) {
        if(!isActive()){
            return Collections.EMPTY_MAP;
        }
        final String moduleKey = key(modules.getModule(module));
        LOG.debug("Getting real configuration for '{}'", moduleKey);

        final Map<String, ConfiguredVariableItem>
                cachedConfiguration = sharedCache.computeIfAbsent(moduleKey, (mk) -> new LinkedHashMap<>())
                // variables which not exists in the cache
                , notCachedConfiguration = new LinkedHashMap<>()
                // variables which value is different with cached variable (returns cached value)
                , updatedVarsConfiguration = new LinkedHashMap<>()
                ;
        // process received module's configuration
        currentConfiguration.entrySet().forEach((entry) -> {
            final String itemPath = entry.getKey();
            final ConfiguredVariableItem currentVarItem = entry.getValue();
            final ConfiguredVariableItem cachedVarItem = cachedConfiguration.get(itemPath);
            if (cachedVarItem == null) {
                LOG.debug("Cached configuration has no key:'{}'", itemPath);
                notCachedConfiguration.put(itemPath, currentVarItem);
            } else if (!cachedVarItem.equals(currentVarItem)) {
                LOG.debug("Cached configuration has different value for key:'{}' input:'{}' cached:'{}'", new Object[]{itemPath, currentVarItem, cachedVarItem});
                updatedVarsConfiguration.put(itemPath, cachedVarItem);
            }
        });
        // store not cached variables
        if (!notCachedConfiguration.isEmpty()) {
            LOG.debug("Adding extra variables to the cached module config");
            newConfiguredVariables(module, notCachedConfiguration);
        }
        return updatedVarsConfiguration;
    }

    /**
     * To update configured variables
     *
     * @param module   the consumer of configuration
     * @param notCachedConfiguration new variables of configuration
     */
    @Override
    public void newConfiguredVariables(HealthItemPK module, Map<String, ConfiguredVariableItem> notCachedConfiguration) {
        // send request to store updates
        sharedQueue.offer(new UpdateConfigurationEvent(module, notCachedConfiguration));

        // updating the cache
        final String moduleKey = key(modules.getModule(module));
        LOG.debug("Update configuration for '{}'", moduleKey);
        final Map<String, ConfiguredVariableItem> cachedConfiguration =
                sharedCache.computeIfAbsent(moduleKey, s -> new LinkedHashMap<>())
                ;
        cachedConfiguration.putAll(notCachedConfiguration);
        LOG.debug("Refresh cache");
        // put updated configuration back to the cache
        sharedCache.put(moduleKey, cachedConfiguration);
    }

    /**
     * To change/replace the configuration of module
     * 
     * @param module configurable module
     * @param configuration new configuration map
     * @return stored configuration
     */
    @Override
    public Map<String, ConfiguredVariableItem> changeConfiguration(HealthItemPK module, Map<String, ConfiguredVariableItem> configuration) {
        // send request to replace configuration
        sharedQueue.offer(new ChangeConfigurationEvent(module, configuration));

        final String moduleKey = key(modules.getModule(module));
        LOG.debug("Replace configuration for '{}'", moduleKey);
        sharedCache.put(moduleKey, configuration);
        return configuration;
    }

    /**
     * To get the list of configurable groups
     *
     * @return the list of key(module)
     */
    @Override
    public List<String> getConfigurableGroups() {
        try {
            return modules.getModules().stream().map(m -> key(m)).collect(Collectors.toList());
        }catch(Throwable t){
            LOG.error("Can't get list of configured groups", t);
            return Collections.<String>emptyList();
        }
    }

    /**
     * To get configuration of module
     *
     * @param configurationGroup module as string
     * @return the configuration
     */
    @Override
    public Map<String, ConfiguredVariableItem> getConfigurationGroup(String configurationGroup) {
        return sharedCache.get(configurationGroup);
    }

    /**
     * To get item by module-id and path
     *
     * @param moduleId module-id as string
     * @param path path to value in configuration map
     * @param value new value of parameter
     * @return stored configuration parameter or null if wrong parameters
     */
    @Override
    public ConfiguredVariableItem updateConfigurationItemByModule(String moduleId, String path, String value) {
        final Map<String,ConfiguredVariableItem> configuration;
        if ((configuration = sharedCache.get(moduleId)) == null){
            return null;
        }
        ConfiguredVariableItem item;
        if ((item = configuration.get(path)) == null){
            String parts[] = path.split("\\.");
            item = new LocalConfiguredVariableItem(parts[parts.length-1], "Ad hoc updated by operator", value );
        }else {
            item.set(value);
        }
        if (item.isValid()) {
            configuration.put(path, item);
        }
        return changeConfiguration(modules.getModule(moduleId), configuration).get(path);
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
    public void configurationChanged(Map<String, ConfiguredVariableItem> changed) {}

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
        return "modulesConfigurationService";
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
        return "Service to support modules configurations";
    }

    public Map<String, Map<String, ConfiguredVariableItem>> getSharedCache() {
        return sharedCache;
    }

    @Override
    public String toString() {
        return "-ModuleConfigurationService-";
    }

    // private methods
    private static String normal(String groupName) {
        return StringUtils.isEmpty(groupName) ? "" : groupName.endsWith(".") ? groupName : groupName + ".";
    }

    private void processStoreEventsQueue(){
        serviceRuns.getAndSet(active = true);
        try {
            while (active) {
                final StoreEvent event = sharedQueue.poll(100, TimeUnit.MILLISECONDS);
                if (event != null) {
                    event.storeData(this);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Distributed Queue's polling was interrupted", e);
        } catch (Throwable e) {
            LOG.error("Unhandled Exception was caught", e);
        } finally {
            serviceRuns.getAndSet(active = false);
            LOG.info("Service stopped.");
        }
    }
    // inner-classes for distributed queue
    private static abstract class StoreEvent implements Serializable{
        protected final ModuleWrapper module;
        protected final Map<String, ConfiguredVariableItem> configuration;
        public StoreEvent(HealthItemPK module, Map<String, ConfiguredVariableItem> configuration){
            this.module = new ModuleWrapper(module);
            this.configuration = new HashMap<>(configuration);
        }
        public abstract void storeData(ModuleConfigurationServiceImpl service);
    }
    private static class UpdateConfigurationEvent extends ModuleConfigurationServiceImpl.StoreEvent {

        public UpdateConfigurationEvent(HealthItemPK module, Map<String, ConfiguredVariableItem> configuration) {
            super(module, configuration);
        }

        @Override
        public void storeData(ModuleConfigurationServiceImpl service) {
            LOG.debug("Storing to configurations storage (version stayed the same)");
            service.storage.storeChangedConfiguration(module, configuration);
        }
    }
    private static class ChangeConfigurationEvent extends ModuleConfigurationServiceImpl.StoreEvent {

        public ChangeConfigurationEvent(HealthItemPK module, Map<String, ConfiguredVariableItem> configuration) {
            super(module, configuration);
        }

        @Override
        public void storeData(ModuleConfigurationServiceImpl service) {
            final String modulePK = key(module);
            LOG.debug("Replace configuration for '{}' (increment version)", modulePK);
            service.sharedCache.put(modulePK, service.storage.replaceConfiguration(module, configuration));
        }
    }
}
