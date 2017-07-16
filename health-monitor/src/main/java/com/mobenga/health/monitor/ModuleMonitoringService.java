package com.mobenga.health.monitor;

import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.MonitoredAction;
import com.mobenga.health.model.transport.ConfiguredVariableItemDto;

/**
 * The service to monitor actions of the module
 */
public interface ModuleMonitoringService extends MonitoredService{
    String PARAMS_PACKAGE = "health.monitor.service.monitored.actions";

    // ignore-modules configurable parameter
    String IGNORE_MODULES_NAME = "ignoreModules";
    ConfiguredVariableItem IGRNORE_MODULES =
            new ConfiguredVariableItemDto(IGNORE_MODULES_NAME, "The set of modules to ignore save actions for.", "none");
    String IGNORE_MODULES_FULL_NAME = PARAMS_PACKAGE + "." + IGNORE_MODULES_NAME;

    // Declare main parameters of the module
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
        return "monitoredActionsService";
    }

    /**
     * to get the value of item's application version
     *
     * @return the value
     */
    @Override
    default public String getVersionId() {
        return "0.1";
    }

    /**
     * to get description of module
     *
     * @return the value
     */
    @Override
    default public String getDescription() {
        return "The service to monitor actions of the module";
    }

    
    /**
     * To create the instance of MonitoredAction.class
     *
     * @return the instance doesn't attached to database
     */
    MonitoredAction createMonitoredAction();

    /**
     * To monitoring the action proceed in the system
     *
     * @param application the owner of action
     * @param action monitored action bean
     */
    void actionMonitoring(ModuleKey application, MonitoredAction action);
}
