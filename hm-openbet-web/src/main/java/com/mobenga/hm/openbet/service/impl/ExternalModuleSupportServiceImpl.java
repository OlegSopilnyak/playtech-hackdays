package com.mobenga.hm.openbet.service.impl;

import com.mobenga.health.model.ConfiguredVariableEntity;
import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.storage.HealthStorage;
import com.mobenga.hm.openbet.dto.ExternalModulePing;
import com.mobenga.hm.openbet.dto.ModuleConfigurationItem;
import com.mobenga.hm.openbet.service.ExternalModuleSupportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * External modules support realization
 */
public class ExternalModuleSupportServiceImpl implements ExternalModuleSupportService, MonitoredService {
    public static final String PARAMS_PACKAGE = "health.monitor.module.external.service";
    private static final ConfiguredVariableItem PARAM1 =
            new LocalConfiguredVariableItem("parameter1", "Example of parameter number", 150);
    private static final ConfiguredVariableItem PARAM2 =
            new LocalConfiguredVariableItem("parameter2", "Example of parameter string", "Hello World");
    public static final String HB_PARAM1_KEY = PARAMS_PACKAGE + "." + PARAM1.getName();
    public static final String HB_PARAM2_KEY = PARAMS_PACKAGE + "." + PARAM2.getName();
    private final Map<String, ConfiguredVariableItem> config = new HashMap<>();
    {
        config.put(HB_PARAM1_KEY, PARAM1);
        config.put(HB_PARAM2_KEY, PARAM2);
    }

    @Autowired
    private ModuleConfigurationService configurationService;

    @Autowired
    private HealthStorage healthStorage;

    @Autowired
    private ModuleStateNotificationService notifier;

    public void initialize(){
        notifier.register(this);
    }
    /**
     * Respond to module's ping
     *
     * @param ping ping from module
     * @return configuration changes
     */
    @Override
    public List<ModuleConfigurationItem> pong(ExternalModulePing ping) {
        ping.getOutput().stream().forEach( (a) -> {System.out.println(" From "+ping.getHost()+" message "+a);});
        HealthItemPK pk = healthStorage.getModulePK(ping.getModulePK());
        Map<String,ConfiguredVariableItem> moduleConfig = new HashMap<>();
        ping.getConfiguration().forEach( (i) -> {moduleConfig.put(i.getPath(), transform(i, ping));});
        Map<String,ConfiguredVariableItem> update = configurationService.getUpdatedVariables(pk,moduleConfig);
        List<ModuleConfigurationItem> response = new ArrayList<>();
        for(ConfiguredVariableItem item : update.values()){
            ModuleConfigurationItem i = new ModuleConfigurationItem();
            i.setValue(item.getValue());
            i.setType(item.getType().name());
            i.setPath(item.getName());
            response.add(i);
        }
        return response;
    }
    private ConfiguredVariableEntity transform(ModuleConfigurationItem item, ExternalModulePing ping){
        ConfiguredVariableEntity entity = new ConfiguredVariableEntity();
        entity.setValue(item.getValue());
        entity.setName(item.getPath());
        entity.setType(ConfiguredVariableItem.Type.STRING);
        entity.setDescription("external module propery");
        entity.setModuleKey(ping.getModulePK());
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

    /**
     * To change the configuration item
     *
     * @param module module-id
     * @param name   item name
     * @param value  item value
     * @return changed item
     */
    @Override
    public ModuleConfigurationItem changeConfigurationItem(String module, String name, String value) {
        ConfiguredVariableItem item = configurationService.updateConfigurationItemByModule(module, name, value);
        if (item != null){
            ModuleConfigurationItem tItem = new ModuleConfigurationItem();
            tItem.setPath(item.getName());
            tItem.setType(item.getType().name());
            tItem.setValue(item.getValue());
            return tItem;
        }
        return null;
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
        return true;
    }

    /**
     * The handle to restart monitored service
     */
    @Override
    public void restart() {

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
        return "RestController for external modules";
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
        return "-ExternalModuleSupportService - ";
    }
}
