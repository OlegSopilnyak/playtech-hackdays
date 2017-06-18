package com.mobenga.health.monitor;

import com.mobenga.health.monitor.behavior.ModuleHealth;
import com.mobenga.health.model.ModulePK;

/**
 * The interface on any monitored service
 */
public interface MonitoredService extends ModuleHealth, ModulePK {
    /**
     * The handle to restart monitored service
     *
     */
    void restart();
}
