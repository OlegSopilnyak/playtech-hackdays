package com.mobenga.health.storage;


import java.util.List;
import com.mobenga.health.model.ModulePK;

/**
 * Heath services data storage
 */
public interface HealthModuleStorage {
    /**
     * Try to save module's information
     *
     * @param module information of module to save
     */
    void save(ModulePK module);

    /**
     * To return the list of stored modules
     *
     * @return list of modules
     */
    List<ModulePK> modulesList();
}
