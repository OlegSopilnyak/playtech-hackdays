package com.mobenga.health.storage;

import com.mobenga.health.model.HealthItemPK;

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
    void save(HealthItemPK module);

    /**
     * To return the list of stored modules
     *
     * @return
     */
    List<HealthItemPK> modulesList();
}
