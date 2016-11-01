package com.mobenga.health.storage.impl;

import com.mobenga.health.model.*;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.health.model.persistence.ValidatingEntity;
import com.mobenga.health.model.transport.ModuleHealthItem;
import com.mobenga.health.monitor.behavior.ModuleHealth;
import com.mobenga.health.storage.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.mobenga.health.HealthUtils.key;
import static com.mobenga.health.storage.impl.ConfiguredVariableItemLightWeightFactory.itemFor;

/**
 * Trivial implementation of system storage
 */
public class SimpleFileStorageImpl implements
        ConfigurationStorage,
        HealthStorage,
        HeartBeatStorage,
        MonitoredActionStorage,
        ModuleOutputStorage
{
    private static final Logger LOG = LoggerFactory.getLogger(SimpleFileStorageImpl.class);
    public static final String DATA_FILE_MODULES = HealthItemPK.STORAGE_NAME + ".properties";
    public static final String DATA_FILE_CONFIG = ConfiguredVariableItem.STORAGE_NAME;
    public static final String DATA_FILE_HEARTBEAT = HeartBeat.STORAGE_NAME;
    public static final String DATA_FILE_ACTIONS = MonitoredAction.STORAGE_NAME;
    private volatile String hostName = "localhost", hostIp = "127.0.0.1";

    private Map<String, StringEntity> configRepo = new ConcurrentHashMap<>();
    private Map<String, Integer> configVersions = new ConcurrentHashMap<>();
    private final ConfiguredVariableEntity configTemplate = new ConfiguredVariableEntity();

    private Map<String, StringEntity> actionRepo = new ConcurrentHashMap<>();
    private final MonitoredActionEntity actionTemplate = new MonitoredActionEntity();
    private Map<String, StringEntity> hbRepo = new ConcurrentHashMap<>();
    private final HealthConditionEntity hbTemplate = new HealthConditionEntity();
    private Properties modules = new Properties();
    private StructureModuleEntity moduleTemplate = new StructureModuleEntity();

    private Map<String, ModuleOutput> moduleOutputMap = new HashMap<>();

    private boolean initialized = false;

    @Autowired
    private UniqueIdGenerator idGenerator;
    @Autowired
    private TimeService timer;

    private File dataFolder = new File(System.getProperty("user.home"));

    @PostConstruct
    public void init() throws Exception {
        synchronized (modules) {
            if(initialized) return;
            hostName = InetAddress.getLocalHost().getHostName();
            hostIp = InetAddress.getLocalHost().getHostAddress();
            reStoreModules();
            configRepo = restoreConfigurationRepository();
            actionRepo = reStoreRepository(DATA_FILE_ACTIONS, actionTemplate);
            hbRepo = reStoreRepository(DATA_FILE_HEARTBEAT, hbTemplate);
            initialized = true;
        }
    }

    /**
     * For tests only
     * @return the value
     */
    boolean isInitialized() {
        return initialized;
    }

    /**
     * To get stored PK by exists PK
     *
     * @param application the PK instance
     * @return the instance
     */
    @Override
    public HealthItemPK getModulePK(HealthItemPK application) {
        return getOrCreateModuleEntity(application);
    }

    /**
     * @param applicationId
     * @return
     */
    @Override
    public HealthItemPK getModulePK(String applicationId) {
        String moduleString = modules.getProperty(applicationId);
        if (moduleString == null){
            HealthItemPK module = new StructureModuleEntity(applicationId);
            modules.setProperty(applicationId, module.toString());
            storeModules();
            return module;
        }
        return (HealthItemPK) moduleTemplate.fromString(moduleString);
    }

    /**
     * To save module's heart-beat
     *
     * @param module state of module
     */
    @Override
    public void saveHeartBeat(ModuleHealth module) {
        final Date nowDateTime = timer.now();
        LOG.debug("Saving module heart-beat time '{}'", nowDateTime);
        Map<String, StringEntity> repository = reStoreRepository(DATA_FILE_HEARTBEAT, hbTemplate);
        String moduleId = key(module.getModulePK());
        LOG.debug("Heart-beat is not stored in this version of storage.");
        String key = moduleId + "."+hostName;
        HealthConditionEntity condition = (HealthConditionEntity) repository.get(key);
        if (condition == null || condition.changed(module)) {
            condition = new HealthConditionEntity();
            condition.setHealthPK(module.getModulePK());
            condition.setHostAddress(hostIp);
            condition.setHostName(hostName);
            condition.setModuleActive(module.isActive());
            repository.put(key, condition);
            storeRepository(DATA_FILE_HEARTBEAT, repository);
            hbRepo = repository;
        }
    }

    void removeModuleHB(HealthItemPK pk){
        Map<String, StringEntity> repository = reStoreRepository(DATA_FILE_HEARTBEAT, hbTemplate);
        final String key = key(pk) + "."+hostName;
        repository.remove(key);
        storeRepository(DATA_FILE_HEARTBEAT, repository);
        hbRepo = repository;
    }

    /**
     * To get states of all modules
     *
     * @return list of states
     */
    @Override
    public List<ModuleHealthItem> getSystemHealth() {
        final List<ModuleHealthItem> systemHealth = new ArrayList<>();
        for(StringEntity entity : hbRepo.values()){
            HealthConditionEntity condition = (HealthConditionEntity) entity;
            if (hostName.equals(condition.getHostName())) {
                systemHealth.add(new ModuleHealthItem(condition.getModule(), condition.isModuleActive()));
            }
        }
        return systemHealth;
    }

    /**
     * To save the state of monitored action
     *
     * @param pk     PK of module
     * @param action action to save
     */
    @Override
    public void saveActionState(HealthItemPK pk, MonitoredAction action) {
        if (!(action instanceof MonitoredActionEntity)) {
            LOG.error("Action instance is not entity '{}'", action);
            return;
        }
        Map<String, StringEntity> repository = reStoreRepository(DATA_FILE_ACTIONS, actionTemplate);
        final MonitoredActionEntity actionEntity = (MonitoredActionEntity) action;
        if (StringUtils.isEmpty(actionEntity.getId())) {
            actionEntity.setId(idGenerator.generate());
            actionEntity.setHealthPK(getOrCreateModuleEntity(pk).getId());
            actionEntity.setHost(hostName);
        }
        actionEntity.validate();
        LOG.debug("Saving '{}'", actionEntity);
        repository.put(actionEntity.getId(), actionEntity);
        storeRepository(DATA_FILE_ACTIONS, repository);
        actionRepo = repository;
    }

    MonitoredAction getAction(MonitoredAction action){
        if (!(action instanceof MonitoredActionEntity)) {
            LOG.error("Action instance is not entity '{}'", action);
            return null;
        }
        final MonitoredActionEntity actionEntity = (MonitoredActionEntity) action;
        return (MonitoredAction) actionRepo.get(actionEntity.getId());
    }

    void removeAction(MonitoredAction action){
        if (!(action instanceof MonitoredActionEntity)) {
            LOG.error("Action instance is not entity '{}'", action);
            return;
        }
        final MonitoredActionEntity actionEntity = (MonitoredActionEntity) action;
        if (StringUtils.isEmpty(actionEntity.getId()))  {
            LOG.warn("Cannot remove not stored '{}'", actionEntity);
            return;
        }
        LOG.debug("Removing '{}'", actionEntity);
        final Map<String, StringEntity> repository = reStoreRepository(DATA_FILE_ACTIONS, actionTemplate);
        repository.remove(actionEntity.getId());
        storeRepository(DATA_FILE_ACTIONS, repository);
        actionRepo = repository;
    }

    /**
     * To create the instance of MonitoredAction.class
     *
     * @return the instance doesn't attached to database
     */
    @Override
    public MonitoredAction createMonitoredAction() {
        return new MonitoredActionEntity();
    }

    /**
     * To change/replace module's configuration
     * <br/> Updates from external system (administrator change the parameters of module)
     *
     * @param module        configurable module
     * @param configuration new configuration
     */
    @Override
    public void replaceConfiguration(HealthItemPK module, Map<String, ConfiguredVariableItem> configuration) {
        LOG.debug("Storing whole configuration for '{}'", module);
        final String moduleKey = key(getModulePK(module));
        final List<ConfiguredVariableEntity> changedItems = new ArrayList<>();
        final ConfiguredVariableEntity root = configTemplate.copy();
        root.setModuleKey(moduleKey);
        root.setVersion(0);
        // increase the version
        try {
            increaseVariablesVersionFor(moduleKey);
        } catch (Throwable e) {
            LOG.error("Can't increase configuration version for '{}'", moduleKey, e);
            return;
        }
        configuration.entrySet().stream().forEach((e) -> {
            final ConfiguredVariableEntity entity = root.copy();
            entity.setId(idGenerator.generate());
            entity.setPackageKey(getPackage(e.getKey()));
            entity.apply(e.getValue());
            try {
                entity.validate();
                changedItems.add(entity);
            } catch (ValidatingEntity.EntityInvalidState ex) {
                LOG.warn("Invalid entity '{}' ignored.", entity, ex);
            }
        });
        if (!changedItems.isEmpty()) {
            storeUpdatedItems(changedItems);
        }

    }

    void removeModuleConfiguration(HealthItemPK module){
        final String moduleId = key(module);
        final Map<String, StringEntity> repository = restoreConfigurationRepository();
        for(Iterator<Map.Entry<String, StringEntity>> i = repository.entrySet().iterator();i.hasNext();){
            Map.Entry<String, StringEntity> entry = i.next();
            if (((ConfiguredVariableEntity)entry.getValue()).getModuleKey().equals(moduleId)){
                i.remove();
            }
        }
        storeConfigurationRepository(repository);
        configRepo = repository;
    }
    /**
     * To store only configuration changes to database
     * <br/> Updates from the module (new module's config parameter was added by author)
     *
     * @param module        the consumer of configuration
     * @param configuration configured variables
     */
    @Override
    public void storeChangedConfiguration(HealthItemPK module, Map<String, ConfiguredVariableItem> configuration) {
        LOG.debug("Storing changed configuration for '{}'", module);
        final String applicationKey = key(getModulePK(module));
        final List<ConfiguredVariableEntity> changedItems = new ArrayList<>();
        final Map<String, ConfiguredVariableItem> actualConfig = this.getConfiguration(applicationKey, 0);
        final ConfiguredVariableEntity root = configTemplate.copy();
        root.setModuleKey(applicationKey);
        root.setVersion(0);
        configuration.entrySet().stream().forEach((e) -> {
            final String mapKey = e.getKey();
            if (!actualConfig.containsKey(mapKey)) {
                // new configuration parameter has received
                final ConfiguredVariableEntity entity = root.copy();
                entity.setId(idGenerator.generate());
                entity.setPackageKey(getPackage(mapKey));
                entity.apply(e.getValue());
                try {
                    entity.validate();
                    changedItems.add(entity);
                } catch (ValidatingEntity.EntityInvalidState ex) {
                    LOG.warn("Invalid entity '{}' ignored.", entity, ex);
                }
            }
        });
        if (!changedItems.isEmpty()) {
            storeUpdatedItems(changedItems);
        }
    }

    /**
     * To get stored list of application PKs
     *
     * @return the list of available PKs
     */
    @Override
    public List<String> getApplicationsPKs() {
        LOG.debug("Getting the list of available modules.");
        try {
            reStoreModules();
            return new ArrayList<>(modules.stringPropertyNames());
        } catch (Throwable e) {
            LOG.error("Can't get modules.", e);
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * To get configuration for particular application
     *
     * @param modulePK application's primary key
     * @return configuration as map
     */
    @Override
    public Map<String, ConfiguredVariableItem> getConfiguration(String modulePK) {
        LOG.debug("Getting actual configuration for '{}'", modulePK);
        return getConfiguration(modulePK, 0);
    }

    /**
     * To get configuration for particular application
     *
     * @param modulePK application's primary key
     * @param version  needed version
     * @return configuration as map
     */
    @Override
    public Map<String, ConfiguredVariableItem> getConfiguration(String modulePK, int version) {
        LOG.debug("Getting {} version of configuration for '{}'", version, modulePK);
        try {
            return configRepo.entrySet()
                    .stream()
                    .map(s -> (ConfiguredVariableEntity) s.getValue())
                    .filter(i -> modulePK.equals(i.getModuleKey()) && version == i.getVersion())
                    .collect(Collectors.toMap(i -> i.getMapKey(), i -> itemFor(modulePK, i.getPackageKey(), i)));
        } catch (Throwable e) {
            LOG.error("Can't get configuration for '{}'", modulePK, e);
            return Collections.EMPTY_MAP;
        }
    }

    /**
     * To create the instance of configured variable item
     *
     * @return detached new instance
     */
    @Override
    public ConfiguredVariableItem createVariableItem() {
        LOG.debug("Making ConfiguredVariableItem instance.");
        return new ConfiguredVariableEntity();
    }

    /**
     * To get the version of configuration for particular module
     *
     * @param modulePK pipeline separated main fields of module
     * @return actual version of configuration
     */
    @Override
    public int getConfigurationVersion(String modulePK) {
        final Integer actualVersion = configVersions.get(modulePK);
        return actualVersion == null ? -1 : actualVersion.intValue();
    }

    /**
     * To get the version of configuration for particular module
     *
     * @param module module instance
     * @return actual version of configuration
     */
    @Override
    public int getConfigurationVersion(HealthItemPK module) {
        return getConfigurationVersion(key(module));
    }

    /**
     * To create module's output item for particular module
     *
     * @param module owner of output
     * @param type   the type of output
     * @return a new instance of output
     */
    @Override
    public ModuleOutput createModuleOutput(HealthItemPK module, String type) {
        final ModuleOutput template = moduleOutputMap.get(type);
        return template == null ? null : template.copy().setModulePK(key(module));
    }

    /**
     * To save chaged module's output
     *
     * @param message output to save
     */
    @Override
    public void saveModuleOutput(ModuleOutput message) {
        LOG.debug("Saving module-output class='{}'", message.getClass().getCanonicalName());
        final ModuleOutput template = moduleOutputMap.get(message.getMessageType());
        if (template instanceof StringEntity){
            final StringEntity entityTemplate = (StringEntity) template.copy();
            final String repositoryName = entityTemplate.storageName();
            final Map<String, StringEntity> repository = reStoreRepository(repositoryName, entityTemplate);
            ((StringEntity)message).setId(idGenerator.generate());
            if (message instanceof ValidatingEntity){
                try {
                    ((ValidatingEntity)message).validate();
                    repository.put(message.getId(), (StringEntity)message);
                    storeRepository(repositoryName, repository);
                    LOG.debug("The module-output saved successfully.");
                } catch (ValidatingEntity.EntityInvalidState ex) {
                    LOG.warn("Invalid entity '{}' ignored.", message, ex);
                }
            }
        }
    }

    /**
     * To select entities suit the criteria
     *
     * @param criteria criteria to select
     * @param offset   required part of selection
     * @return required
     */
    @Override
    public Page<ModuleOutput> select(ModuleOutput.Criteria criteria, Pageable offset) {
        final List<ModuleOutput> selected = new ArrayList<>();
        final int maxMessagesQuantity = offset == null ? Integer.MAX_VALUE : offset.getPageSize();
        if (StringUtils.isEmpty(criteria.getType())){
            // to evaluate all types of messages
            for(final ModuleOutput message: moduleOutputMap.values()){
                walkThroughRepository(criteria, selected, (StringEntity) message, maxMessagesQuantity);
            }
        }else {
            // to evaluate particular type of messages
            walkThroughRepository(criteria, selected, (StringEntity) moduleOutputMap.get(criteria.getType()), maxMessagesQuantity);
        }
        return new PageImpl<ModuleOutput>(selected, offset, selected.size());
    }

    /**
     * To delete unnecessary entities
     *
     * @param criteria criteria of selection
     * @return the quantity of deleted entities
     */
    @Override
    public int delete(ModuleOutput.Criteria criteria) {
        final int deleted[] = {0};
        if (StringUtils.isEmpty(criteria.getType())){
            // walk through all types
            for(final ModuleOutput message: moduleOutputMap.values()){
                final StringEntity template = (StringEntity) message;
                walkAndDeleteThroughRepository(criteria, template, deleted);
            }
        }else {
            // walk through the particular type
            final StringEntity template = (StringEntity) moduleOutputMap.get(criteria.getType());
            walkAndDeleteThroughRepository(criteria, template, deleted);
        }
        return deleted[0];
    }

    public Map<String, ModuleOutput> getModuleOutputMap() {
        return moduleOutputMap;
    }

    public void setModuleOutputMap(Map<String, ModuleOutput> moduleOutputMap) {
        this.moduleOutputMap = moduleOutputMap;
    }

    // private methods
    private void walkAndDeleteThroughRepository(ModuleOutput.Criteria criteria,StringEntity template, int counter[]){
        if (template == null) return;
        final Map<String, StringEntity> repository = reStoreRepository(template.storageName(), template);
        final Map<String, StringEntity> newRepo = repository.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof ModuleOutput)
                .filter(entry -> !criteria.isSuitable((ModuleOutput) entry.getValue()))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry-> entry.getValue()));
        storeRepository(template.storageName(), newRepo);
        counter[0] += repository.size() - newRepo.size();
    }
    private void walkThroughRepository(ModuleOutput.Criteria criteria, List<ModuleOutput> selected, StringEntity template, int maxItem) {
        if (template == null) return;
        final Map<String, StringEntity> repository = reStoreRepository(template.storageName(), template);
        final int []added = {0};
        repository.values().stream()
                .filter(item -> maxItem < added[0])
                .filter(item -> item instanceof ModuleOutput)
                .filter(item -> criteria.isSuitable((ModuleOutput) item))
                .forEach((item) -> {selected.add((ModuleOutput)item); added[0]++;});
    }

    @NotNull
    private static String getPackage(String mapKey) {
        final String[] pack = mapKey.split("\\.");
        final StringBuilder builder = new StringBuilder(pack[0]);
        for (int i = 1; i < pack.length - 1; i++) {
            builder.append(".").append(pack[i]);
        }
        return builder.toString();
    }

    void removeModule(HealthItemPK module){
        LOG.debug("Removing module {}.", module);
        reStoreModules();
        modules.remove(key(module));
        storeModules();
    }

    @Nullable
    private StructureModuleEntity getOrCreateModuleEntity(HealthItemPK module) {
        LOG.debug("Finding entity for '{}'", module);
        try {
            reStoreModules();
            StructureModuleEntity template = moduleTemplate;
            for (String id : modules.stringPropertyNames()) {
                final StructureModuleEntity entity = (StructureModuleEntity) template.fromString(modules.getProperty(id));
                if (entity.equals(module)) return entity;
            }
            final String moduleId = key(module);
            template = new StructureModuleEntity(module);
            template.setId(moduleId);
            modules.setProperty(moduleId, template.toString());
            storeModules();
            return template;
        } catch (Throwable e) {
            LOG.error("Can't find module for '{}'", module, e);
            return null;
        }
    }
    private void storeModules() {
        try {
            Writer dataChannel = new FileWriter(new File(dataFolder,DATA_FILE_MODULES));
            modules.store(dataChannel, "registered modules");
            dataChannel.close();
        } catch (Exception e) {
            LOG.error("Can't save ", e);
        }
    }

    private void reStoreModules() {
        try {
            modules.clear();
            Reader dataChannel = new FileReader(new File(dataFolder,DATA_FILE_MODULES));
            modules.load(dataChannel);
            dataChannel.close();
        } catch (Exception e) {
            LOG.error("Can't load modules, storing empty.", e);
            storeModules();
        }
    }

    private void storeRepository(String config, Map<String, StringEntity> configRepo) {
        try {
            Properties props = new Properties();
            configRepo.entrySet().forEach((i) -> {
                props.setProperty(i.getKey(), i.getValue().toString());
            });
            Writer dataChannel = new FileWriter(new File(dataFolder,config + ".properties"));
            props.store(dataChannel, "Storage of beans " + config);
            dataChannel.close();
        } catch (Exception e) {
            LOG.error("Can't save repo " + config, e);
        }
    }

    private Map<String, StringEntity> reStoreRepository(String config, StringEntity template) {
        Map<String, StringEntity> configRepo = new ConcurrentHashMap<>();
        try {
            Properties props = new Properties();
            Reader dataChannel = new FileReader(new File(dataFolder,config + ".properties"));
            props.load(dataChannel);
            dataChannel.close();
            for (String id : props.stringPropertyNames()) {
                configRepo.put(id, template.fromString(props.getProperty(id)));
            }
            return configRepo;
        } catch (Exception e) {
            LOG.error("Can't read repo " + config + " storing default storage.", e);
            storeRepository(config, configRepo);
        }
        return new ConcurrentHashMap<>();
    }

    private void increaseVariablesVersionFor(String moduleKey) {
        final Map<String, StringEntity> repository = restoreConfigurationRepository();
        final AtomicBoolean changed = new AtomicBoolean(false);
        repository.entrySet().forEach((cfg)-> {
            final ConfiguredVariableEntity entity = (ConfiguredVariableEntity) cfg.getValue();
            if (moduleKey.equals(entity.getModuleKey())){
                changed.getAndSet(true);
                entity.setVersion(entity.getVersion() + 1);
            }
        });
        if (changed.get()) {
            storeConfigurationRepository(repository);
        }
    }

    private void storeUpdatedItems(List<ConfiguredVariableEntity> changedItems) {
        try {
            final Map<String, StringEntity> repository = restoreConfigurationRepository();
            changedItems.forEach(item -> {repository.put(item.getId(), item);});
            storeConfigurationRepository(repository);
        } catch (Throwable e) {
            LOG.error("Can't save configuration updates.", e);
        }
    }

    private void storeConfigurationRepository(Map<String, StringEntity> repository) {
        storeRepository(DATA_FILE_CONFIG, repository);
        recalculateConfigurationVersions(repository);
        configRepo = repository;
    }

    private  Map<String, StringEntity> restoreConfigurationRepository(){
        final Map<String, StringEntity> repository = reStoreRepository(DATA_FILE_CONFIG, configTemplate);
        recalculateConfigurationVersions(repository);
        return repository;
    }

    private void recalculateConfigurationVersions(Map<String, StringEntity> repository) {
        configVersions.clear();
        repository.values().stream().map(e -> (ConfiguredVariableEntity)e).forEach(c -> {
            final String moduleKey = c.getModuleKey();
            final Integer version = c.getVersion();
            final Integer currentVersion = configVersions.computeIfAbsent(moduleKey, key -> version);
            configVersions.put(moduleKey, version > currentVersion ? version : currentVersion);
        });
    }
}
