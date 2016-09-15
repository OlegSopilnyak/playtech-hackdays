package com.mobenga.health.monitor.behavior;

import com.mobenga.health.model.ConfiguredVariableItem;

import java.util.Map;

/**
 * Interface means tha service can publish configuration and accept configuration changes
 */
public interface Configurable {
    /**
     * To get current configuration of module
     *
     * @return the map
     */
    Map<String, ConfiguredVariableItem> getConfiguration();

    /**
     * Notification about change configuration
     *
     * @param changed map with changes
     */
    void configurationChanged(Map<String, ConfiguredVariableItem> changed);
}
