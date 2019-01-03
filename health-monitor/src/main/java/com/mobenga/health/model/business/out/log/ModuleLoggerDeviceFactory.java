package com.mobenga.health.model.business.out.log;

import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.model.business.out.ModuleOutputDevice;
import com.mobenga.health.model.transport.ConfiguredVariableItemDto;
import com.mobenga.health.monitor.MonitoredService;

/**
 * Logger facility for monitored modules
 */
public interface ModuleLoggerDeviceFactory extends ModuleOutputDevice.Factory, MonitoredService {

    // names of configured parameter "ignoreModules"
    String PARAMS_PACKAGE = "registry.monitor.service.module.output.log";
    String IGNORE_MODULES_NAME = "ignoreModules";
    ConfiguredVariableItem IGNORE_MODULES
            = new ConfiguredVariableItemDto(IGNORE_MODULES_NAME, "The set of modules to ignore logging for.", "none");
    // canonical name of configured parameter
    String IGNORE_MODULES_FULL_NAME = PARAMS_PACKAGE + "." + IGNORE_MODULES_NAME;

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
        return "modulesOuputService";
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
        return "Module for log-messages";
    }

    /**
     * Class-logger device to manage log output in context of current monitored
     * action
     */
}
