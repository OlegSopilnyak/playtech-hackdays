package com.mobenga.health.model;

import java.util.Date;

/**
 * Logical Model Bean to keep information about monitored action
 */
public abstract class MonitoredAction {
    // the name of storage item (table/index-type/etc)
    public static final String STORAGE_NAME = "monitored-action";
    // the NULL Action may used as a marker
    public static MonitoredAction NULL = new NullAction();

    /**
     * To make the copy of action
     *
     * @return the copy
     */
    public abstract MonitoredAction copy();

    /**
     * To get the Id (primary key) of monitored action
     *
     * @return the value or null if not saved yet
     */
    public abstract String getId();

    /**
     * The reference to healthPK
     *
     * @return value of PK
     */
    public abstract String getHealthPK();

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
    public abstract State getState();

    /**
     * To get the time when action starts
     *
     * @return the value (not null)
     */
    public abstract Date getStart();

    /**
     * To get the time when action has finished
     *
     * @return the value (may be null)
     */
    public abstract Date getFinish();

    /**
     * To get the duration of proceeded action
     *
     * @return the value (nanoseconds)
     */
    public abstract long getDuration();

    /**
     * To get the name of host where action proceeded
     *
     * @return the value (not null)
     */
    public abstract String getHost();

    /**
     * To update the description value
     *
     * @param description new value
     */
    public abstract void setDescription(String description);

    /**
     * To update the time when action starts
     *
     * @param starts new value
     */
    public abstract void setStart(Date starts);

    /**
     * To update the current state of the action
     *
     * @param state new value
     */
    public abstract void setState(State state);

    /**
     * To update the time when action has finished
     *
     * @param ends new value
     */
    public abstract void setFinish(Date ends);

    /**
     * To update the duration of proceeded action
     *
     * @param duration new value (nanoseconds)
     */
    public abstract void setDuration(long duration);

    /**
     * To update the value of host
     *
     * @param hostName the host-name
     */
    public abstract void setHost(String hostName);
    // inner classes
    // available states of action in lifecycle
    public enum State{
        INIT, // initialization phase
        PROGRESS, // proceeding phase
        SUCCESS, // action finished well
        FAIL // something bad happeded during action's proceeding
    }
    // class for null action
    private static class NullAction extends MonitoredAction{
        @Override
        public MonitoredAction copy() {
            return null;
        }
        @Override
        public String getId() {
            return null;
        }
        @Override
        public String getHealthPK() {
            return null;
        }
        @Override
        public String getDescription() {
            return null;
        }
        @Override
        public State getState() {
            return null;
        }
        @Override
        public Date getStart() {
            return null;
        }
        @Override
        public Date getFinish() {
            return null;
        }
        @Override
        public long getDuration() {
            return 0;
        }
        @Override
        public String getHost() {
            return null;
        }
        @Override
        public void setDescription(String description) {}
        @Override
        public void setStart(Date starts) {}
        @Override
        public void setState(State state) {}
        @Override
        public void setFinish(Date ends) {}
        @Override
        public void setDuration(long duration) {}
        @Override
        public void setHost(String hostName) {}
    }
}
