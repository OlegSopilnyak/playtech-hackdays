package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.transport.ConfiguredVariableItemDto;
import com.mobenga.health.model.transport.ModuleKeyDto;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.HealthModuleService;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.storage.ConfigurationStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mobenga.health.HealthUtils.key;

/**
 * Realization of module configuration
 *
 * @see ModuleConfigurationService
 */
public class ModuleConfigurationServiceImpl extends AbstractRunningService implements ModuleConfigurationService {

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

    private final Map<String, ConfiguredVariableItem> config = new LinkedHashMap<>();
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
        return "-ModuleConfigurationService-";
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    public void initialize() {
        super.start();
    }

    @Override
    protected void beforeStart() {
        final Map<String, Map<String, ConfiguredVariableItem>> sharedMap = distributed.map(sharedMapName);
        if (sharedMap.isEmpty()) {
            LOG.info("It seems node is alone. Loading stored configurations.");
            modules.getModules().stream()
                    .map(module -> key(module))
                    .forEach(moduleId -> sharedMap.putIfAbsent(moduleId, storage.getConfiguration(moduleId)));
        }
        sharedCache = sharedMap;
        sharedQueue = distributed.queue(sharedQueueName);
    }

    @Override
    protected void afterStart() {
        notifier.register(this);
    }

    @Override
    protected void serviceLoopIteration() throws InterruptedException {
        if (!isActive()) {
            return;
        }

        StoreEvent event;
        while (isActive() && (event = sharedQueue.poll(100, TimeUnit.MILLISECONDS)) != null) {
            event.storeData(this);
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
    }

    @Override
    protected void afterStop() {
        notifier.unRegister(this);
    }

    /**
     * To get the configuration of module
     *
     * @param module    the consumer of configurations
     * @param groupName the dot-delimited name of group (empty is root)
     * @return map of full-qualified configured variables
     */
    @Override
    public Map<String, ConfiguredVariableItem> getConfigurationGroup(ModuleKey module, String groupName) {
        final String normalizedGroup = normal(groupName);
        final String moduleId = key(modules.getModule(module));
        LOG.debug("Getting configuration for '{}' from group '{}'", new Object[]{moduleId, normalizedGroup});
        return sharedCache.computeIfAbsent(moduleId, m -> new LinkedHashMap<>()).entrySet().stream()
                .filter(parameter -> parameter.getKey().startsWith(normalizedGroup))
                .collect(Collectors.toMap(param -> param.getKey(), param -> param.getValue()));
    }

    /**
     * To get updated configured variables
     *
     * @param module               the consumer of configuration
     * @param currentConfiguration current state of configuration
     * @return updated variables (emptyMap if none)
     */
    @Override
    public Map<String, ConfiguredVariableItem> getUpdatedVariables(ModuleKey module, Map<String, ConfiguredVariableItem> currentConfiguration) {
        if (!isActive()) {
            return Collections.EMPTY_MAP;
        }

        // let's start
        final String moduleId = key(modules.getModule(module));
        LOG.debug("Getting real configuration for '{}'", moduleId);

        // cached (actual) configuration of the module
        final Map<String, ConfiguredVariableItem>
                cachedConfiguration = sharedCache.computeIfAbsent(moduleId, m -> new LinkedHashMap<>());

        // variables which not exists in the cache
        final Map<String, ConfiguredVariableItem> notCachedConfiguration = new LinkedHashMap<>();
        // variables which value is different with cached variable (returns cached value)
        final Map<String, ConfiguredVariableItem> updatedVarsConfiguration = new LinkedHashMap<>();

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
     * @param module                 the consumer of configuration
     * @param notCachedConfiguration new variables of configuration
     */
    @Override
    public void newConfiguredVariables(ModuleKey module, Map<String, ConfiguredVariableItem> notCachedConfiguration) {
        // send request to store updates
        sharedQueue.offer(new UpdateConfigurationEvent(module, notCachedConfiguration));

        // updating the cache
        final String moduleId = key(modules.getModule(module));
        LOG.debug("Update configuration for '{}'", moduleId);
        final Map<String, ConfiguredVariableItem> cachedConfiguration =
                sharedCache.computeIfAbsent(moduleId, s -> new LinkedHashMap<>());
        cachedConfiguration.putAll(notCachedConfiguration);
        LOG.debug("Refresh cache");
        // put updated configuration back to the cache
        sharedCache.put(moduleId, cachedConfiguration);
    }

    /**
     * To change/replace the configuration of module
     *
     * @param module        configurable module
     * @param configuration new configuration map
     * @return stored configuration
     */
    @Override
    public Map<String, ConfiguredVariableItem> changeConfiguration(ModuleKey module, Map<String, ConfiguredVariableItem> configuration) {
        // send request to replace configuration
        sharedQueue.offer(new ChangeConfigurationEvent(module, configuration));

        final String moduleId = key(modules.getModule(module));
        LOG.debug("Replace configuration for '{}'", moduleId);
        sharedCache.put(moduleId, configuration);
        return configuration;
    }

    /**
     * To get the list of configurable groups
     *
     * @return the list of module-ids
     */
    @Override
    public List<String> getConfigurableGroups() {
        try {
            return modules.getModules().stream().map(m -> key(m)).collect(Collectors.toList());
        } catch (Throwable t) {
            LOG.error("Can't get list of configured groups", t);
            return Collections.<String>emptyList();
        }
    }

    /**
     * To get configuration of module
     *
     * @param moduleId module-id as string
     * @return the configuration
     */
    @Override
    public Map<String, ConfiguredVariableItem> getConfigurationGroup(String moduleId) {
        return sharedCache.get(moduleId);
    }

    /**
     * To get item by module-id and path
     *
     * @param moduleId module-id as string
     * @param path     path to value in configuration map
     * @param value    new value of parameter
     * @return stored configuration parameter or null if wrong parameters
     */
    @Override
    public ConfiguredVariableItem updateConfigurationItemByModule(String moduleId, String path, String value) {
        final Map<String, ConfiguredVariableItem> configuration;
        if ((configuration = sharedCache.get(moduleId)) == null) {
            return null;
        }
        ConfiguredVariableItem item;
        if ((item = configuration.get(path)) == null) {
            String parts[] = path.split("\\.");
            item = new ConfiguredVariableItemDto(parts[parts.length - 1], "Ad hoc updated by operator", value);
        } else {
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
    }

    public Map<String, Map<String, ConfiguredVariableItem>> getSharedCache() {
        return sharedCache;
    }

    // private methods
    private static String normal(String groupName) {
        return StringUtils.isEmpty(groupName) ? "" : groupName.endsWith(".") ? groupName : groupName + ".";
    }

    // inner-classes for distributed queue
    // Base class storage update
    private static abstract class StoreEvent implements Serializable {

        private static final long serialVersionUID = -3402253351758269847L;
        protected final ModuleKeyDto module;
        protected final Map<String, ConfiguredVariableItem> configuration;

        public StoreEvent(ModuleKey module, Map<String, ConfiguredVariableItem> configuration) {
            this.module = new ModuleKeyDto(module);
            this.configuration = new HashMap<>(configuration);
        }

        public abstract void storeData(final ModuleConfigurationServiceImpl service);
    }

    // class-event to update storage
    private static class UpdateConfigurationEvent extends StoreEvent {

        private static final long serialVersionUID = 3246466013912817229L;

        public UpdateConfigurationEvent(ModuleKey module, Map<String, ConfiguredVariableItem> configuration) {
            super(module, configuration);
        }

        // updating storage
        @Override
        public void storeData(final ModuleConfigurationServiceImpl service) {
            LOG.debug("Storing to configurations storage (version stayed the same)");
            service.storage.storeChangedConfiguration(module, configuration);
        }
    }

    // class-event for change storage
    private static class ChangeConfigurationEvent extends StoreEvent {

        private static final long serialVersionUID = 1282128459869589211L;

        public ChangeConfigurationEvent(ModuleKey module, Map<String, ConfiguredVariableItem> configuration) {
            super(module, configuration);
        }

        // data substituted by new one
        @Override
        public void storeData(final ModuleConfigurationServiceImpl service) {
            final String moduleId = key(module);
            LOG.debug("Replace configuration for '{}' (increment version)", moduleId);
            service.sharedCache.put(moduleId, service.storage.replaceConfiguration(module, configuration));
        }
    }
}
