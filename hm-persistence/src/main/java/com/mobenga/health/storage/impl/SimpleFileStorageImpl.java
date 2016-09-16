package com.mobenga.health.storage.impl;

import com.mobenga.health.model.*;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.health.model.persistence.ValidatingEntity;
import com.mobenga.health.model.transport.ModuleHealthItem;
import com.mobenga.health.monitor.behavior.ModuleHealth;
import com.mobenga.health.storage.ConfigurationStorage;
import com.mobenga.health.storage.HealthStorage;
import com.mobenga.health.storage.HeartBeatStorage;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mobenga.health.HealthUtils.key;
import static com.mobenga.health.storage.impl.ConfiguredVariableItemLightWeightFactory.itemFor;
import static java.lang.Package.getPackage;

/**
 * Trivial implementation of system storage
 */
public class SimpleFileStorageImpl implements ConfigurationStorage, HealthStorage, HeartBeatStorage, MonitoredActionStorage {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleFileStorageImpl.class);
    private volatile String hostName = "localhost", hostIp = "127.0.0.1";

    private Map<String, StringEntity> configRepo = new ConcurrentHashMap<>();
    private Map<String, StringEntity> actionRepo = new ConcurrentHashMap<>();
    private Properties modules = new Properties();

    @Autowired
    private UniqueIdGenerator idGenerator;
    @Autowired
    private TimeService timer;

    private File dataFolder = new File(System.getProperty("user.home"));

    @PostConstruct
    public void init() throws Exception {
        hostName = InetAddress.getLocalHost().getHostName();
        hostIp = InetAddress.getLocalHost().getHostAddress();
        reStoreModules();
        reStoreRepository("config", new ConfiguredVariableEntity());
        reStoreRepository("actions", new MonitoredActionEntity());
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
        return (HealthItemPK) new StructureModuleEntity().fromString(moduleString);
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
        StructureModuleEntity modulePK = getOrCreateModuleEntity(module.getModulePK());
        String moduleId = modulePK.getId();

    }

    /**
     * To get states of all modules
     *
     * @return list of states
     */
    @Override
    public List<ModuleHealthItem> getSystemHealth() {
        return null;
    }

    /**
     * To save the state of monitored action
     *
     * @param pk     PK of module
     * @param action action to save
     */
    @Override
    public void saveActionState(HealthItemPK pk, MonitoredAction action) {
        final MonitoredActionEntity actionEntity = (MonitoredActionEntity) action;
        if (StringUtils.isEmpty(actionEntity.getId())) {
            actionEntity.setHealthPK(getOrCreateModuleEntity(pk).getId());
            actionEntity.setHost(hostName);
        }
        actionEntity.validate();
        LOG.debug("Saving '{}'", actionEntity);
        actionRepo.put(actionEntity.getId(), actionEntity);
        storeRepository("actions", actionRepo);
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
     *
     * @param module        configurable module
     * @param configuration new configuration
     */
    @Override
    public void replaceConfiguration(HealthItemPK module, Map<String, ConfiguredVariableItem> configuration) {
        LOG.debug("Storing whole configuration for '{}'", module);
        final String moduleKey = key(module);
        final List<ConfiguredVariableEntity> changedItems = new ArrayList<>();
        final ConfiguredVariableEntity root = new ConfiguredVariableEntity();
        root.setModuleKey(moduleKey);
        root.setVersion(0);
//        // increase the version
//        try {
//            increaseVariablesVersionFor(moduleKey);
//        } catch (Throwable e) {
//            LOG.error("Can't increase configuration version for '{}'", moduleKey, e);
//            return;
//        }
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
            try {
                changedItems.forEach((item) -> {
                    configRepo.put(item.getId(), item);
                });
                storeRepository("config", configRepo);
//                configRepository.save(changedItems);
            } catch (Throwable e) {
                LOG.error("Can't save configuration updates.", e);
            }
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
        try {
            Map<String, StringEntity> configRepo = new ConcurrentHashMap<>();
            Properties props = new Properties();
            Reader dataChannel = new FileReader(new File(dataFolder,config + ".properties"));
            props.load(dataChannel);
            dataChannel.close();
            for (String id : props.stringPropertyNames()) {
                configRepo.put(id, template.fromString(props.getProperty(id)));
            }
            return configRepo;
        } catch (Exception e) {
            LOG.error("Can't read repo " + config, e);
        }
        return new ConcurrentHashMap<>();
    }

    private void storeModules() {
        try {
            Writer dataChannel = new FileWriter(new File(dataFolder,"modules.properties"));
            modules.store(dataChannel, "registered modules");
            dataChannel.close();
        } catch (Exception e) {
            LOG.error("Can't save ", e);
        }
    }

    private void reStoreModules() {
        try {
            Reader dataChannel = new FileReader(new File(dataFolder,"modules.properties"));
            modules.load(dataChannel);
            dataChannel.close();
        } catch (Exception e) {
            LOG.error("Can't load ", e);
        }
    }

    /**
     * To store the changed configuration to database
     *
     * @param module        the consumer of configuration
     * @param configuration configured variables
     */
    @Override
    public void storeChangedConfiguration(HealthItemPK module, Map<String, ConfiguredVariableItem> configuration) {
        LOG.debug("Storing changed configuration for '{}'", module);
        final String applicationKey = key(module);
        final List<ConfiguredVariableEntity> changedItems = new ArrayList<>();
        final Map<String, ConfiguredVariableItem> dbConfig = getConfiguration(applicationKey);
        final ConfiguredVariableEntity root = new ConfiguredVariableEntity();
        root.setModuleKey(applicationKey);
        root.setVersion(0);
        configuration.entrySet().stream().forEach((e) -> {
            final String mapKey = e.getKey();
            if (dbConfig.get(mapKey) == null) {
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
            try {
                changedItems.forEach((item) -> {
                    configRepo.put(item.getId(), item);
                });
                storeRepository("config", configRepo);
//                configRepository.save(changedItems);
            } catch (Throwable e) {
                LOG.error("Can't save configuration updates.", e);
            }
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
            configRepo = reStoreRepository("config", new ConfiguredVariableEntity());
            return configRepo.entrySet().stream().filter(s -> modulePK.equals(((ConfiguredVariableEntity) s.getValue()).getModuleKey()))
                    .map(s -> (ConfiguredVariableEntity) s.getValue())
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

    private static String getPackage(String mapKey) {
        final String[] pack = mapKey.split("\\.");
        final StringBuilder builder = new StringBuilder(pack[0]);
        for (int i = 1; i < pack.length - 1; i++) {
            builder.append(".").append(pack[i]);
        }
        return builder.toString();
    }

    private StructureModuleEntity getOrCreateModuleEntity(HealthItemPK module) {
        LOG.debug("Finding entity for '{}'", module);
        try {
            reStoreModules();
            StructureModuleEntity template = new StructureModuleEntity();
            for (String id : modules.stringPropertyNames()) {
                StructureModuleEntity entity = (StructureModuleEntity) template.fromString(modules.getProperty(id));
                if (
                        entity.getSystemId().equals(module.getSystemId()) &&
                                entity.getApplicationId().equals(module.getApplicationId()) &&
                                entity.getVersionId().equals(module.getVersionId())
                        ) {
                    return entity;
                }
            }
            template = new StructureModuleEntity(module);
            modules.setProperty(key(module), template.toString());
            storeModules();
            return template;
        } catch (Throwable e) {
            LOG.error("Can't find module for '{}'", module, e);
            return null;
        }
    }
}
