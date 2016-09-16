package com.mobenga.hm.openbet.service.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.hm.openbet.dto.ExternalModulePing;
import com.mobenga.hm.openbet.dto.ModuleConfigurationItem;
import com.mobenga.hm.openbet.service.ExternalModuleSupportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * External modules support realization
 */
public class ExternalModuleSupportServiceImpl implements ExternalModuleSupportService {

    @Autowired
    private ModuleConfigurationService configurationService;

    /**
     * Respond to module's ping
     *
     * @param ping ping from module
     * @return configuration changes
     */
    @Override
    public List<ModuleConfigurationItem> pong(ExternalModulePing ping) {
        return Collections.EMPTY_LIST;
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
}
