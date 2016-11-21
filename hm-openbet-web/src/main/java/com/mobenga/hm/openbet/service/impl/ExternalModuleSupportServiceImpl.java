package com.mobenga.hm.openbet.service.impl;

import com.mobenga.health.model.*;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.monitor.*;
import com.mobenga.health.storage.HeartBeatStorage;
import com.mobenga.health.storage.ModuleOutputStorage;
import com.mobenga.health.storage.MonitoredActionStorage;
import com.mobenga.hm.openbet.dto.ConfigurationUpdate;
import com.mobenga.hm.openbet.dto.ExternalModulePing;
import com.mobenga.hm.openbet.dto.ModuleConfigurationItem;
import com.mobenga.hm.openbet.service.DateTimeConverter;
import com.mobenga.hm.openbet.service.ExternalModuleSupportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.mobenga.health.HealthUtils.key;

/**
 * External modules support realization
 */
public class ExternalModuleSupportServiceImpl implements ExternalModuleSupportService, MonitoredService {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalModuleSupportServiceImpl.class);
    public static final String PARAMS_PACKAGE = "health.monitor.module.external.service";
    private static final ConfiguredVariableItem PARAM1 =
            new LocalConfiguredVariableItem("parameter1", "Example of parameter number", 150);
    private static final ConfiguredVariableItem PARAM2 =
            new LocalConfiguredVariableItem("parameter2", "Example of parameter string", "Hello World");
    public static final String HB_PARAM1_KEY = PARAMS_PACKAGE + "." + PARAM1.getName();
    public static final String HB_PARAM2_KEY = PARAMS_PACKAGE + "." + PARAM2.getName();
    private final Map<String, ConfiguredVariableItem> config = new HashMap<>();
    private BlockingQueue<ExternalModulePing> sharedQueue;

    @Autowired
    @Qualifier("serviceRunner")
    private ExecutorService executor;
    private final AtomicBoolean serviceRuns = new AtomicBoolean(false);
    private volatile boolean active;

    public ExternalModuleSupportServiceImpl() {
        config.put(HB_PARAM1_KEY, PARAM1);
        config.put(HB_PARAM2_KEY, PARAM2);
    }

    @Autowired
    private HealthModuleService modules;

    @Autowired
    private ModuleConfigurationService configurationService;

    @Autowired
    private ModuleStateNotificationService notifier;

    @Autowired
    private HeartBeatStorage hbStorage;

    @Autowired
    private DistributedContainersService distributed;

    @Autowired
    private MonitoredActionStorage actionStorage;

    @Autowired
    private ModuleOutputStorage outputStorage;
    @Autowired
    private DateTimeConverter dt;

//    @Autowired
//    private MonitoredActionStorage actionStorage;

    @Value("${configuration.shared.external.queue.name:'external-modules-queue'}")
    private String sharedQueueName;

    public void initialize(){
        if (active) return;
        sharedQueue = distributed.queue(sharedQueueName);
        notifier.register(this);
        active = false;
        executor.submit(()->postponedStorePing());
        while(!serviceRuns.get());
        LOG.info("Service started.");
    }

    public void shutdown(){
        if (!active) return;
        notifier.unRegister(this);
        active = false;
        while(serviceRuns.get());
        LOG.info("Service stopped.");
    }
    /**
     * Respond to module's ping
     *
     * @param ping ping from module
     * @return configuration changes
     */
    @Override
    public List<ModuleConfigurationItem> pong(ExternalModulePing ping) {
        // postpone work with storages
        sharedQueue.offer(ping);
        // processing configuration section
        final HealthItemPK pk = modules.getModule(ping.getModule());

        LOG.debug("Processing module configuration items:'{}'", ping.getConfiguration().size());

        final Map<String,ConfiguredVariableItem> moduleConfig = new HashMap<>();
        ping.getConfiguration().forEach( cvi -> {moduleConfig.put(cvi.getPath(), transform(cvi, ping));});
        final Map<String,ConfiguredVariableItem> updated = configurationService.getUpdatedVariables(pk, moduleConfig);
        final List<ModuleConfigurationItem> response = new ArrayList<>();

        LOG.debug("Return '{}' updated configuration's items.", updated.size());

        updated.entrySet().forEach(e -> {
            ModuleConfigurationItem dtoItem = new ModuleConfigurationItem(e.getKey(), e.getValue().getType().name(), e.getValue().getValue());
            dtoItem.setDescription(e.getValue().getDescription());
            response.add(dtoItem);
        });
        return response;
    }

    /**
     * To change the configuration item
     *
     * @param module module-id
     * @param path points divided full path to item in configuration map
     * @param value item's new value
     * @return changed item
     */
    @Override
    public ModuleConfigurationItem changeConfigurationItem(String module, String path, String value) {
        LOG.debug("Request to change for '{}' path '{}' to value :'{}'", module, path, value);
        final ConfiguredVariableItem item = configurationService.updateConfigurationItemByModule(module, path, value);
        if (item == null) {
            return null;
        }
        final ModuleConfigurationItem dtoItem = new ModuleConfigurationItem();
        dtoItem.setPath(path);
        dtoItem.setType(item.getType().name());
        dtoItem.setValue(item.getValue());
        return dtoItem;
    }

    /**
     * To change the module's configuration
     *
     * @param update request
     * @return updated module configuration items
     */
    @Override
    public List<ModuleConfigurationItem> changeConfiguration(ConfigurationUpdate update) {
        LOG.debug("Request batch change configuration from '{}' for '{}'", update.getHost(), update.getModule());
        HealthItemPK module = update.getModule();
        Map<String, ConfiguredVariableItem> updated, updating = new HashMap<>();
        update.getUpdated().forEach(item -> {
            String pack[] = item.getPath().split("\\.");
            LocalConfiguredVariableItem i = new LocalConfiguredVariableItem(pack[pack.length-1], "Updated item", item.getValue());
            i.setType(ConfiguredVariableItem.Type.valueOf(item.getType()));
            if (i.isValid()) {
                updating.put(item.getPath(), i);
            }else {
                LOG.error("Invalid item '{}' received.", item);
            }
        });
        updated = configurationService.changeConfiguration(module, updating);
        LOG.debug("New configuration size is '{}'", updated.size());
        return
                updated.entrySet().stream().map(e -> {
                    final ModuleConfigurationItem item = new ModuleConfigurationItem();
                    item.setPath(e.getKey());
                    item.setType(e.getValue().getType().name());
                    item.setValue(e.getValue().getValue());
                    return item;
                }).collect(Collectors.toList());
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
        return "externalModule";
    }

    /**
     * to get the value of item's application
     *
     * @return the value
     */
    @Override
    public String getApplicationId() {
        return "restController";
    }

    /**
     * to get the value of item's application version
     *
     * @return the value
     */
    @Override
    public String getVersionId() {
        return "0.2";
    }

    /**
     * to get description of module
     *
     * @return the value
     */
    @Override
    public String getDescription() {
        return "Service to work with external modules";
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

    @Override
    public String toString() {
        return "-ExternalModuleSupportService-";
    }
    // private methods
    private void postponedStorePing(){
        serviceRuns.getAndSet(active = true);
        try {
            while (active) {
                final ExternalModulePing ping = sharedQueue.poll(100, TimeUnit.MILLISECONDS);
                if (ping != null) {
                    storePing(ping);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Work with queue was interrupted ", e);
        }catch (Throwable e){
            LOG.error("Work with queue was interrupted ", e);
        } finally {
            serviceRuns.getAndSet(active = false);
        }
    }
    private void storePing(ExternalModulePing ping){
        LOG.debug("Processing ping from '{}'", ping.getHost());
        final HealthItemPK pk = modules.getModule(ping.getModule());
        final String moduleKey = key(pk);
        // process heart-beat
        LOG.debug("Processing state of module '{}'", ping.getState());
        hbStorage.saveModuleState(pk, "Active".equalsIgnoreCase(ping.getState()));
        // process output
        LOG.debug("Processing '{}' messages of raw log.", ping.getOutput().size());
        ping.getOutput().forEach(o->{
            LogMessage msg = (LogMessage) outputStorage.createModuleOutput(pk, LogMessage.OUTPUT_TYPE);
            msg.setWhenOccured(dt.asDate(o.getWhenOccurred()));
            msg.setModulePK(moduleKey);
            msg.setPayload(o.getPayload());
            outputStorage.saveModuleOutput(msg);
        });
        // process output with actions
        LOG.debug("Processing '{}' actions of action log.", ping.getActions().size());
        ping.getActions().forEach(a->{
            final MonitoredAction action = actionStorage.createMonitoredAction();
            action.setDescription(a.getDescription());
            action.setStart(dt.asDate(a.getStartTime()));
            action.setFinish(dt.asDate(a.getFinishTime()));
            action.setDuration(a.getDuration());
            action.setState(MonitoredAction.State.valueOf(a.getState()));
            actionStorage.saveActionState(pk, action);
            final String actionId = action.getId();
            LOG.debug("Processing '{}' output logs for Action '{}'.", a.getOutput().size(), a.getName());
            a.getOutput().forEach(o->{
                LogMessage msg = (LogMessage) outputStorage.createModuleOutput(pk, LogMessage.OUTPUT_TYPE);
                msg.setActionId(actionId);
                msg.setWhenOccured(dt.asDate(o.getWhenOccurred()));
                msg.setModulePK(moduleKey);
                msg.setPayload(o.getPayload());
                outputStorage.saveModuleOutput(msg);
            });
        });
    }
    private ConfiguredVariableEntity transform(ModuleConfigurationItem item, ExternalModulePing ping){
        final ConfiguredVariableEntity entity = new ConfiguredVariableEntity();
        final String [] path = item.getPath().split("\\.");
        entity.setValue(item.getValue());
        entity.setName(path[path.length-1]);
        entity.setType(ConfiguredVariableItem.Type.STRING);
        entity.setDescription(item.getDescription());
        entity.setModuleKey(key(ping.getModule()));
        entity.setPackageKey(getPackage(item.getPath()));
        entity.setVersion(0);
        return entity;
    }
    private static String getPackage(String mapKey) {
        final String[] pack = mapKey.split("\\.");
        final StringBuilder builder = new StringBuilder(pack[0]);
        for (int i = 1; i < pack.length - 1; i++) {
            builder.append(".").append(pack[i]);
        }
        return builder.toString();
    }

}
