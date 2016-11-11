package com.mobenga.health.model;

import java.io.Serializable;

/**
 * Interface of PK for any Health items
 */
//@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface HealthItemPK extends Serializable {
    // the name of storage item (table/index-type/etc)
    String STORAGE_NAME = "health-pk";
    /**
     * to get the value of item's system
     * @return the value
     */
    String getSystemId();

    /**
     * to get the value of item's application
     * @return the value
     */
    String getApplicationId();

    /**
     * to get the value of item's application version
     * @return the value
     */
    String getVersionId();

    /**
     * to get description of module
     * @return the value
     */
    String getDescription();
}
