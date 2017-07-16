package com.mobenga.health.model.business.out;

import java.util.Date;

/**
 * The criteria of module's output selection
 */
public interface SelectOutputCriteria {
    /**
     * Select one entity with appropriate id
     *
     * @return the value of id or null if ignored
     */
    String getOutputId();
    /**
     * select all entities with appropriate type
     *
     * @return the value of type or null if ignored
     */
    String getType();
    /**
     * Select all entities of particular module
     *
     * @return the value or null if ignored
     */
    String getModulePK();

    /**
     * Select all entities which belong enumerated actions
     *
     * @return the array of action-id or empty if ignored
     */
    String[] getActionIds();

    /**
     * select all entities which happened after the date
     *
     * @return the value or null if ignored
     */
    Date getMoreThan();

    /**
     * select all entities which happened before the date
     *
     * @return the value or null if ignored
     */
    Date getLessThen();

    /**
     * To test is particular message suitable for the criteria
     *
     * @param message message to test
     * @return true if message is suitable
     */
    boolean isSuitable(ModuleOutputMessage message);
}

