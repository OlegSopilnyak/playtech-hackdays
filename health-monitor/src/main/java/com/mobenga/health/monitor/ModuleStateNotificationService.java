package com.mobenga.health.monitor;

import com.mobenga.health.model.transport.ModuleHealthItem;

import java.util.List;

/**
 * The service to notify about state of registered modules
 */
public interface ModuleStateNotificationService {
    /**
     * To register module
     *
     * @param module module to watch
     */
    void register(MonitoredService module);
    /**
     * To un-register module
     *
     * @param module module to watch
     */
    void unRegister(MonitoredService module);

    /**
     * To get states of all registered modules
     *
     * @return the list of module states
     */
    List<ModuleHealthItem> getSystemHealth();
}
