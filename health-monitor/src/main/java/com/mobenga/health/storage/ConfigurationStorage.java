package com.mobenga.health.storage;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;

import java.util.List;
import java.util.Map;

/**
 * The storage to work with configuration stuff
 */
public interface ConfigurationStorage {
    /**
     * To change/replace module's configuration
     * 
     * @param module configurable module
     * @param configuration new configuration
     */
    void replaceConfiguration(HealthItemPK module, Map<String, ConfiguredVariableItem> configuration);
    
    /**
     * To store the changed configuration to database
     *
     * @param application the consumer of configuration
     * @param configuration configured variables
     */
    void storeChangedConfiguration(HealthItemPK application, Map<String, ConfiguredVariableItem> configuration);

    /**
     * To get stored list of application PKs
     *
     * @return the list of available PKs
     */
    List<String> getApplicationsPKs();

    /**
     * To get configuration for particular application
     *
     * @param applicationPK application's primary key
     * @return configuration as map
     */
    Map<String,ConfiguredVariableItem> getConfiguration(String applicationPK);
    /**
     * To get configuration for particular application
     *
     * @param applicationPK application's primary key
     * @param version needed version
     * @return configuration as map
     */
    Map<String,ConfiguredVariableItem> getConfiguration(String applicationPK, int version);

    /**
     * To create the instance of configured variable item
     *
     * @return detached new instance
     */
    ConfiguredVariableItem createVariableItem();

}
