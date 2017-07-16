package com.mobenga.health.storage.stub;

import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.MonitoredAction;
import com.mobenga.health.model.transport.MonitoredActionDto;
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
    public void saveActionState(ModuleKey pk, MonitoredAction action) {

    }

    /**
     * To create the instance of MonitoredAction.class
     *
     * @return the instance doesn't attached to database
     */
    @Override
    public MonitoredAction createMonitoredAction() {
        return new MonitoredActionDto();
    }
}
