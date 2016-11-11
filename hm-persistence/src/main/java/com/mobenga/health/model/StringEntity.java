package com.mobenga.health.model;

import java.io.Serializable;

/**
 * Mark for persistence
 */
public interface StringEntity extends Serializable {
    /**
     * To store new value of id
     *
     * @param id new value
     */
    void setId(String id);
    /**
     * The name of storage for this sort of beans
     *
     * @return the name
     */
    String storageName();
    /**
     * To convert bean to string
     * @return string
     */
    String toString();

    /**
     * To restore bean from string
     * @param value string-represent bean
     * @return restored bean
     */
    StringEntity fromString(String value);
}
