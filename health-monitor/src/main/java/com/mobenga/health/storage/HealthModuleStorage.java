package com.mobenga.health.storage;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.transport.ModuleWrapper;

import java.util.List;

/**
 * Heath services data storage
 */
public interface HealthModuleStorage {
//    /**
//     * To get stored PK by exists PK
//     *
//     * @param application the PK instance
//     * @return the instance
//     */
//    HealthItemPK getModulePK(HealthItemPK application);
//
//    /**
//     *
//     * @param applicationId
//     * @return
//     */
//    HealthItemPK getModulePK(String applicationId);

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
