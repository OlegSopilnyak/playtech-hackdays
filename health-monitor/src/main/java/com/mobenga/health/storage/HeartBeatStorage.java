package com.mobenga.health.storage;

import com.mobenga.health.model.business.ModuleHealth;
import com.mobenga.health.model.business.ModuleKey;

import java.util.List;

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
    void saveModuleState(ModuleKey module, boolean isActive);

    /**
     * To get states of all modules
     *
     * @return list of states
     */
    List<ModuleHealth> getSystemHealth();
}
