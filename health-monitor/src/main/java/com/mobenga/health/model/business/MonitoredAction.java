package com.mobenga.health.model.business;

import java.io.Serializable;
import java.util.Date;

/**
 * Logical Model Bean to keep information about monitored action
 */
public interface MonitoredAction extends Cloneable,Serializable {
    // the name of storage item (table/index-type/etc)
    String STORAGE_NAME = "monitored-action";

    /**
     * To make the copy of action
     *
     * @return the copy
     */
    MonitoredAction copy();

    /**
     * To get the Id (primary key) of monitored action
     *
     * @return the value or null if not saved yet
     */
    String getId();

    /**
     * The reference to ModuleKey
     *
     * @return value of module key
     */
    String getModuleKey();

    /**
     * To get the description of the action
     *
     * @return the value (not null)
     */
    public abstract String getDescription();

    /**
     * To get the current state of the action
     *
     * @return the value (not null)
     */
    State getState();

    /**
     * To get the time when action starts
     *
     * @return the value (not null)
     */
    Date getStart();

    /**
     * To get the time when action has finished
     *
     * @return the value (may be null)
     */
    Date getFinish();

    /**
     * To get the duration of proceeded action
     *
     * @return the value (nanoseconds)
     */
    long getDuration();

    /**
     * To get the name of host where action proceeded
     *
     * @return the value (not null)
     */
    String getHost();

    // inner classes
    // available states of action in lifecycle
    enum State{
        INIT, // initialization phase
        PROGRESS, // proceeding phase
        SUCCESS, // action finished well
        FAIL // something bad happeded during action's proceeding
    }
}
