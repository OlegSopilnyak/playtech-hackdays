package com.mobenga.health.monitor;

import com.mobenga.health.model.business.ModuleHealth;
import com.mobenga.health.monitor.behavior.Configurable;

/**
 * The interface on any monitored service
 */
public interface MonitoredService extends ModuleHealth, Configurable {
    /**
     * The handle to restart monitored service
     *
     */
    void restart();
}
