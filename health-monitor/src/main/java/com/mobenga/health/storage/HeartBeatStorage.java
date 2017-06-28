package com.mobenga.health.storage;

import com.mobenga.health.model.transport.ModuleHealthDto;
import com.mobenga.health.monitor.behavior.ModuleHealth;

import java.util.List;
import com.mobenga.health.model.ModulePK;

/**
 * The storage to work with hear-beat stuff
 */
public interface HeartBeatStorage {

    /**
     * To save module's heart-beat
     *
     * @param module state of module
     */
    void saveHeartBeat(ModuleHealth module);

    /**
     * To save the module's state
     *
     * @param module checked module
     * @param isActive flag oi module active
     */
    void saveModuleState(ModulePK module, boolean isActive);

    /**
     * To get states of all modules
     *
     * @return list of states
     */
    List<ModuleHealthDto> getSystemHealth();
}
