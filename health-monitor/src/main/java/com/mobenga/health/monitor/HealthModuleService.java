package com.mobenga.health.monitor;

import com.mobenga.health.model.HealthItemPK;

import java.util.List;

/**
 * The service to manage modules
 */
public interface HealthModuleService {
    /**
     * To get cached module by real module
     *
     * @param module real module of application
     * @return the wrapper of module
     */
    HealthItemPK getModule(HealthItemPK module);

    /**
     * To gte cached module by module's key
     *
     * @param moduleKey module's key
     * @return the wrapper of module
     */
    HealthItemPK getModule(String moduleKey);

    /**
     * To get the list of all registered modules
     *
     * @return list of wrappers of modules
     */
    List<HealthItemPK> getModules();
}
