package com.mobenga.health.storage;

import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.model.business.ModuleKey;

import java.util.Map;

/**
 * The storage to work with configuration stuff
 */
public interface ConfigurationStorage {
    /**
     * To change/replace module's configuration
     *  @param module configurable module
     * @param configuration new configuration
     * @return lastVersion of configuration
     */
    Map<String, ConfiguredVariableItem> replaceConfiguration(ModuleKey module, Map<String, ConfiguredVariableItem> configuration);
    
    /**
     * To store the changed configuration to database
     *
     * @param application the consumer of configuration
     * @param configuration configured variables
     */
    void storeChangedConfiguration(ModuleKey application, Map<String, ConfiguredVariableItem> configuration);

    /**
     * To get configuration for particular application
     *
     * @param modulePK application's primary key
     * @return configuration as map
     */
    Map<String,ConfiguredVariableItem> getConfiguration(String modulePK);
    /**
     * To get configuration for particular application
     *
     * @param modulePK application's primary key
     * @param version needed version
     * @return configuration as map
     */
    Map<String,ConfiguredVariableItem> getConfiguration(String modulePK, int version);

    /**
     * To create the instance of configured variable item
     *
     * @return detached new instance
     */
    ConfiguredVariableItem createVariableItem();

    /**
     * To get the version of configuration for particular module
     *
     * @param modulePK pipeline separated main fields of module
     * @return actual version of configuration
     */
    int getConfigurationVersion(String modulePK);

    /**
     * To get the version of configuration for particular module
     *
     * @param module module instance
     * @return actual version of configuration
     */
    int getConfigurationVersion(ModuleKey module);
}
