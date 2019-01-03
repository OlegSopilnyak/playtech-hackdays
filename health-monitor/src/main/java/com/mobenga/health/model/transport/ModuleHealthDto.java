package com.mobenga.health.model.transport;

import com.mobenga.health.model.business.ModuleHealth;
import com.mobenga.health.model.business.ModuleKey;

import java.io.Serializable;

/**
 * Transport object for module's registry
 */
public class ModuleHealthDto extends ModuleKeyDto implements ModuleHealth, Serializable{
    private static final long serialVersionUID = -2049959624478697903L;

    // current condition of module
    private ModuleHealth.Condition condition;
    private Throwable lastMistake;
    private boolean active;


    public ModuleHealthDto() {
    }

    public ModuleHealthDto(ModuleKey module, ModuleHealth.Condition condition) {
        super(module);
        this.condition = condition;
    }

    /**
     * To get the registry condition of module for the moment
     *
     * @returnn current condition value
     */
    @Override
    public Condition getCondition() {
        return condition;
    }

    /**
     * To get last throwable object
     *
     * @return mistake or null if none
     */
    @Override
    public Throwable getLastMistake() {
        return lastMistake;
    }

    /**
     * To check is module active (is working)
     *
     * @return true if module is working
     */
    @Override
    public boolean isActive() {
        return active;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public void setLastMistake(Throwable lastMistake) {
        this.lastMistake = lastMistake;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
