package com.mobenga.health.model.business;

/**
 * The condition of module health at the moment
 */
public interface ModuleHealth extends ModuleKey {
    /**
     * To get the health condition of module for the moment
     *
     * @return current condition value
     */
    Condition getCondition();

    /**
     * To get last throwable object
     *
     * @return mistake or null if none
     */
    Throwable getLastMistake();

    /**
     * To check is module active (is working)
     *
     * @return true if module is working
     */
    boolean isActive();
    /**
     * The health condition of the module at the moment
     */
    enum Condition {
        VERY_GOOD,
        GOOD,
        AVERAGE,
        POOR,
        FAIL
        ;
    }
}
