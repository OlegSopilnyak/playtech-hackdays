package com.mobenga.hm.openbet.service;

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
     * @param name item name
     * @param value item value
     * @return changed item
     */
    ModuleConfigurationItem changeConfigurationItem(String module, String name, String value);
}
