package com.mobenga.health.monitor;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HeartBeat;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.model.transport.ModuleHealthItem;

import java.util.List;

/**
 * The service to notify about state of registered modules
 */
public interface ModuleStateNotificationService extends MonitoredService {
    String PARAMS_PACKAGE = "health.monitor.service.heartbeat";
    
    // HeartBeat delay for Modules StateNotificationService
    String HB_DELAY_NAME = "delay";
    ConfiguredVariableItem HB_DELAY = 
            new LocalConfiguredVariableItem(HB_DELAY_NAME, "The delay between heart beats", HeartBeat.DELAY);
    String HB_DELAY_FULL_NAME = PARAMS_PACKAGE + "." + HB_DELAY_NAME;
    
    /**
     * to get the value of item's system
     *
     * @return the value
     */
    @Override
    default public String getSystemId() {
        return "healthMonitor";
    }

    /**
     * to get the value of item's application
     *
     * @return the value
     */
    @Override
    default public String getApplicationId() {
        return "serviceStateScanner";
    }

    /**
     * to get the value of item's application version
     *
     * @return the value
     */
    @Override
    default public String getVersionId() {
        return "0.1";
    }

    /**
     * to get description of module
     *
     * @return the value
     */
    @Override
    default public String getDescription() {
        return "The service to notify about changes state of registered modules";
    }

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
