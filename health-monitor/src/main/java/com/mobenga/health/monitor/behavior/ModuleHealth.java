package com.mobenga.health.monitor.behavior;

import com.mobenga.health.model.ModulePK;

/**
 * Behavior interface
 */
public interface ModuleHealth extends Configurable {
    /**
     * To get the value of Module's PK
     *
     * @return value of PK (not null)
     */
    ModulePK getModulePK();
    /**
     * Describe the state of module
     *
     * @return true if module active
     */
    boolean isActive();

}
