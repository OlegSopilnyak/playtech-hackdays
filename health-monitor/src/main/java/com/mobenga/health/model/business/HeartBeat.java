package com.mobenga.health.model.business;

import java.util.Date;

/**
 * HeartBeat model declaration
 */
public interface HeartBeat {
    // the name of storage item (table/index-type/etc)
    String STORAGE_NAME = "heart-beat";
    // default delay (milliseconds) between heart-beats
    int DELAY = 2000;

    /**
     * The reference to healthPK
     *
     * @return value of PK
     */
    String getModuleKeyPK();

    /**
     * Exact date-time of heard-beat
     *
     * @return the time
     */
    Date getTime();

    /**
     * The host-name where heard beats
     *
     * @return the host-name
     */
    String getHostName();

    /**
     * The host-address where heard beats
     *
     * @return the host-address
     */
    String getHostAddress();

    /**
     * Condition of module's registry
     *
     * @return value
     */
    ModuleHealth.Condition getCondition();
}
