package com.mobenga.health.storage.stub;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.monitor.impl.MonitoredActionStub;
import com.mobenga.health.storage.MonitoredActionStorage;

/**
 * The stub for MonitoredActionStorage
 */
public class MonitoredActionStorageStub implements MonitoredActionStorage {
    /**
     * To save the state of monitored action
     *
     * @param pk     PK of module
     * @param action action to save
     */
    @Override
    public void saveActionState(HealthItemPK pk, MonitoredAction action) {

    }

    /**
     * To create the instance of MonitoredAction.class
     *
     * @return the instance doesn't attached to database
     */
    @Override
    public MonitoredAction createMonitoredAction() {
        return new MonitoredActionStub();
    }
}
