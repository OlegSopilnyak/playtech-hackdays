package com.mobenga.health.monitor;

import com.mobenga.health.model.ConfiguredVariableItem;

import java.util.List;
import java.util.Map;
import com.mobenga.health.model.ModulePK;

/**
 * Service to support modules configurations
 */
public interface ModuleConfigurationService extends MonitoredService {
    /**
     * to get the value of item's system
     *
     * @return the value
     */
    @Override
    default public String getSystemId() {
        return "healthMonitor";
    } 
    /**
     * to get the value of item's application
     *
     * @return the value
     */
    @Override
    default public String getApplicationId() {
        return "modulesConfigurationService";
    }
    
    /**
     * to get the value of item's application version
     *
     * @return the value
     */
    @Override
    default public String getVersionId() {
        return "0.01";
    }

    /**
     * to get description of module
     *
     * @return the value
     */
    @Override
    default public String getDescription() {
        return "Service to support modules configurations";
    }
    /**
     * To get the configuration of application
     *
     * @param application the consumer of configurations
     * @param groupName the dot-delimited name of group (empty is root)
     * @return map of full-qualified configured variables
     */
    Map<String, ConfiguredVariableItem> getConfigurationGroup(ModulePK application, String groupName);

    /**
     * To get updated configured variables
     *
     * @param application the consumer of configuration
     * @param configuration current state of configuration
     * @return updated variables (emptyMap if none)
     */
    Map<String, ConfiguredVariableItem> getUpdatedVariables(ModulePK application, Map<String, ConfiguredVariableItem> configuration);

    /**
     * To update configured variables
     *
     * @param application the consumer of configuration
     * @param configuration new variables
     */
    void newConfiguredVariables(ModulePK application, Map<String, ConfiguredVariableItem> configuration);
    
    /**
     * To change/replace the configuration of module
     * 
     * @param application configurable module
     * @param configuration new configuration map
     * @return saved configuration
     */
    Map<String, ConfiguredVariableItem> changeConfiguration(ModulePK application, Map<String, ConfiguredVariableItem> configuration);

    /**
     * To get the list of configurable groups
     *
     * @return the list of key(module)
     */
    List<String> getConfigurableGroups();

    /**
     * To get configuration of module
     *
     * @param configurationGroup module as string
     * @return the configuration
     */
    Map<String,ConfiguredVariableItem> getConfigurationGroup(String configurationGroup);

    /**
     * To get item by module-id and name
     *
     * @param module the modulePK
     * @param name the name of item
     * @param value new value of item
     * @return updated variable item
     */
    ConfiguredVariableItem updateConfigurationItemByModule(String module, String name, String value);
}
