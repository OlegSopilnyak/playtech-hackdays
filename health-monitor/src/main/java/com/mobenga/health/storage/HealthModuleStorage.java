package com.mobenga.health.storage;


import com.mobenga.health.model.business.ModuleKey;

import java.util.List;

/**
 * Heath services data storage
 */
public interface HealthModuleStorage {
    /**
     * Try to save module's information
     *
     * @param module information of module to save
     */
    void save(ModuleKey module);

    /**
     * To return the list of stored modules
     *
     * @return list of modules
     */
    List<ModuleKey> modulesList();
}
