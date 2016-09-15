package com.mobenga.health.storage;

import com.mobenga.health.model.HealthItemPK;

/**
 * Heath services data storage
 */
public interface HealthStorage {
    /**
     * To get stored PK by exists PK
     *
     * @param application the PK instance
     * @return the instance
     */
    HealthItemPK getModulePK(HealthItemPK application);
}
