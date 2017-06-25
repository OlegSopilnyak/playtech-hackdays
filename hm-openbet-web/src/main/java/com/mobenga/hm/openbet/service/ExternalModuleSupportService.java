package com.mobenga.hm.openbet.service;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.hm.openbet.dto.ConfigurationUpdate;
import com.mobenga.hm.openbet.dto.ExternalModulePing;
import com.mobenga.hm.openbet.dto.ModuleConfigurationItem;
import static com.mobenga.hm.openbet.service.impl.ExternalModuleSupportServiceImpl.PARAMS_PACKAGE;

import java.util.List;

/**
 * Service to support external modules
 */
public interface ExternalModuleSupportService {
    
    // namespace of service's parameters
    String PARAMS_PACKAGE = "health.monitor.module.external.service";
    // the value of service's parameter
    ConfiguredVariableItem PARAM1 = new LocalConfiguredVariableItem("parameter1", "Example of parameter number", 150);
    // the value of service's parameter
    ConfiguredVariableItem PARAM2 = new LocalConfiguredVariableItem("parameter2", "Example of parameter string", "Hello World");
    // Canonical name of service's parameter
    String HB_PARAM1_KEY = PARAMS_PACKAGE + "." + PARAM1.getName();
    // Canonical name of service's parameter
    String HB_PARAM2_KEY = PARAMS_PACKAGE + "." + PARAM2.getName();
    
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
