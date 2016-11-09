package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.monitor.*;
import com.mobenga.health.storage.ConfigurationStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.*;
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

    @Value("${configuration.shared.config.map.name:'modules-configuration'}")
    private String sharedMapName;

    @Autowired
    private DistributedContainersService distributed;

    @Autowired
    private ConfigurationStorage storage;

    @Autowired
    private HealthModuleService modules;

    @Autowired
    private ModuleStateNotificationService notifier;

    private final Map<String, ConfiguredVariableItem> config = new HashMap<>();

    public void initialize(){
        final Map<String, Map<String, ConfiguredVariableItem>> sharedMap = distributed.map(sharedMapName);
        if (sharedMap.isEmpty()){
            LOG.info("It seems node is alone. Loading stored configurations.");
            try {
                modules.getModules().stream().map(m->key(m))
                        .forEach(module -> sharedMap.putIfAbsent(module, storage.getConfiguration(module)));
            } catch (Throwable t) {
                LOG.error("Basic configuration not initialized.", t);
            }
        }
        sharedCache = sharedMap;
        notifier.register(this);
    }

    public void shutdown(){
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
        LOG.debug("Getting configuration for '{}' from group '{}'", new Object[]{key(module), normalizedGroup});
        final Map<String, ConfiguredVariableItem> cachedConfiguration =
                sharedCache.computeIfAbsent(key(module), (s) -> new LinkedHashMap<>());
        return cachedConfiguration
                .entrySet()
                .stream()
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
        LOG.debug("Getting real configuration for '{}'", new Object[]{key(module)});
        final Map<String, ConfiguredVariableItem> cachedConfiguration = sharedCache.computeIfAbsent(key(module), (s) -> new LinkedHashMap<>());
        final Map<String, ConfiguredVariableItem> notCachedConfiguration = new LinkedHashMap<>();
        final Map<String, ConfiguredVariableItem> updatedVarsConfiguration = new LinkedHashMap<>();
        currentConfiguration.entrySet().stream().forEach((current) -> {
            final ConfiguredVariableItem cachedVarItem = cachedConfiguration.get(current.getKey());
            if (cachedVarItem == null) {
                LOG.debug("Cached configuration has no key:'{}'", current.getKey());
                notCachedConfiguration.put(current.getKey(), current.getValue());
            } else if (!cachedVarItem.equals(current.getValue())) {
                LOG.debug("Cached configuration has different value for key:'{}' input:'{}' cached:'{}'", new Object[]{current.getKey(), current.getValue(), cachedVarItem});
                updatedVarsConfiguration.put(current.getKey(), cachedVarItem);
            }
        });
        if (!notCachedConfiguration.isEmpty()) {
            LOG.debug("Adding values to the cache");
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
        final String modulePK = key(module);
        LOG.debug("Update configuration for '{}'", modulePK);
        final Map<String, ConfiguredVariableItem> cachedConfiguration = sharedCache.computeIfAbsent(modulePK, s -> new LinkedHashMap<>());
        cachedConfiguration.putAll(notCachedConfiguration);
        LOG.debug("Storing to configurations storage");
        storage.storeChangedConfiguration(module, notCachedConfiguration);
        LOG.debug("Refresh cache");
        sharedCache.put(modulePK, cachedConfiguration);
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
        final String modulePK = key(module);
        LOG.debug("Replace configuration for '{}'", modulePK);
        final Map<String, ConfiguredVariableItem> current = storage.replaceConfiguration(module, configuration);
        sharedCache.put(modulePK, current);
        return current;
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
     * @return stored configuratio parameter or null if wrong parameters
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
            item = new LocalConfiguredVariableItem(parts[parts.length-1], "Updated by operator", value );
        }else {
            item.set(value);
        }
        configuration.put(path, item);
        return changeConfiguration(modules.getModule(moduleId), configuration).get(path);
    }

    public Map<String, Map<String, ConfiguredVariableItem>> getSharedCache() {
        return sharedCache;
    }

    public void setSharedCache(Map<String, Map<String, ConfiguredVariableItem>> sharedCache) {
        this.sharedCache = sharedCache;
    }


    // private methods
    private static String normal(String groupName) {
        return StringUtils.isEmpty(groupName) ? "" : groupName.endsWith(".") ? groupName : groupName + ".";
    }

    /**
     * The handle to restart monitored service
     */
    @Override
    public void restart() {
        LOG.info("Restarting...");
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
        return sharedCache != null;
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

    @Override
    public String toString() {
        return "-ModuleConfigurationService-";
    }

}
