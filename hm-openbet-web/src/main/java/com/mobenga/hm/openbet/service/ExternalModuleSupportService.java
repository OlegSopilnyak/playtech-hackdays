package com.mobenga.hm.openbet.service;

import com.mobenga.hm.openbet.dto.ConfigurationUpdate;
import com.mobenga.hm.openbet.dto.ExternalModulePing;
import com.mobenga.hm.openbet.dto.ModuleConfigurationItem;

import java.util.List;

/**
 * Service to support external modules
 */
public interface ExternalModuleSupportService {
    /**
     * Respond to module's ping
     * @param ping ping from module
     * @return configuration changes
     */
    List<ModuleConfigurationItem> pong(ExternalModulePing ping);

    /**
     * To change the configuration item
     *
     * @param module module-id
     * @param path points divided full path to item in configuration map
     * @param value item's new value
     * @return changed item
     */
    ModuleConfigurationItem changeConfigurationItem(String module, String path, String value);

    /**
     * To change the module's configuration
     *
     * @param update request
     * @return updated module configuration items
     */
    List<ModuleConfigurationItem> changeConfiguration(ConfigurationUpdate update);
}
