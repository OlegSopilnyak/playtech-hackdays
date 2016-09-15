package com.mobenga.health.monitor;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.MonitoredAction;

/**
 * The service to monitor actions of the module
 */
public interface ModuleMonitoringService {
    /**
     * To create the instance of MonitoredAction.class
     *
     * @return the instance doesn't attached to database
     */
    MonitoredAction createMonitoredAction();

    /**
     * To monitoring the action proceed in the system
     *
     * @param application the owner of action
     * @param action monitored action bean
     */
    void actionMonitoring(HealthItemPK application, MonitoredAction action);
}
