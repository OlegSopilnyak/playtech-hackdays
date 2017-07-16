package com.mobenga.health.monitor;


import com.mobenga.health.model.business.ModuleKey;

import java.util.List;

/**
 * The service to manage modules
 */
public interface HealthModuleService extends MonitoredService{

    /**
     * to get the value of item's system
     *
     * @return the value
     */
    @Override
    default String getSystemId() {
        return "healthMonitor";
    }

    /**
     * to get the value of item's application
     *
     * @return the value
     */
    @Override
    default String getApplicationId() {
        return "healthModulesManagement";
    }

    /**
     * to get the value of item's application version
     *
     * @return the value
     */
    @Override
    default String getVersionId() {
        return "0.01";
    }

    /**
     * to get description of module
     *
     * @return the value
     */
    @Override
    default String getDescription() {
        return "Module to make distributed access to modules set.";
    }

    /**
     * To get cached module by real module
     *
     * @param module real module of application
     * @return the wrapper of module
     */
    ModuleKey getModule(ModuleKey module);

    /**
     * To get cached module by module's key
     *
     * @param moduleId module's key
     * @return the wrapper of module
     */
    ModuleKey getModule(String moduleId);

    /**
     * To get the list of all registered modules
     *
     * @return list of wrappers of modules
     */
    List<ModuleKey> getModules();
}
