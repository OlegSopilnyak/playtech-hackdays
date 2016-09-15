package com.mobenga.health.monitor;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.monitor.behavior.ModuleHealth;

/**
 * The interface on any monitored service
 */
public interface MonitoredService extends ModuleHealth, HealthItemPK {
    /**
     * The handle to restart monitored service
     *
     */
    void restart();
}
