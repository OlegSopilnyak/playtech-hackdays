package com.mobenga.health.monitor;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;

import java.util.List;
import java.util.Map;

/**
 * Service to support modules configurations
 */
public interface ModuleConfigurationService {
    /**
     * To get the configuration of application
     *
     * @param application the consumer of configurations
     * @param groupName the dot-delimited name of group (empty is root)
     * @return map of full-qualified configured variables
     */
    Map<String, ConfiguredVariableItem> getConfigurationGroup(HealthItemPK application, String groupName);

    /**
     * To get updated configured variables
     *
     * @param application the consumer of configuration
     * @param configuration current state of configuration
     * @return updated variables (emptyMap if none)
     */
    Map<String, ConfiguredVariableItem> getUpdatedVariables(HealthItemPK application, Map<String, ConfiguredVariableItem> configuration);

    /**
     * To update configured variables
     *
     * @param application the consumer of configuration
     * @param configuration new variables
     */
    void newConfiguredVariables(HealthItemPK application, Map<String, ConfiguredVariableItem> configuration);
    
    /**
     * To change/replace the configuration of module
     * 
     * @param application configurable module
     * @param configuration new configuration map
     * @return saved configuration
     */
    Map<String, ConfiguredVariableItem> changeConfiguration(HealthItemPK application, Map<String, ConfiguredVariableItem> configuration);

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
     * @param module
     * @param name
     * @param value
     * @return
     */
    ConfiguredVariableItem updateConfigurationItemByModule(String module, String name, String value);
}
