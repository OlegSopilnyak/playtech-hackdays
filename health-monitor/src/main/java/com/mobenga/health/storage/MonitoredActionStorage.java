package com.mobenga.health.storage;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.MonitoredAction;

/**
 * The storage to work with monitored actions stuff
 */
public interface MonitoredActionStorage {

    /**
     * To save the state of monitored action
     *
     * @param pk PK of module
     * @param action action to save
     */
    void saveActionState(HealthItemPK pk, MonitoredAction action);


    /**
     * To create the instance of MonitoredAction.class
     *
     * @return the instance doesn't attached to database
     */
    MonitoredAction createMonitoredAction();
}
