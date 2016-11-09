package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.MonitoredAction;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * The monitored-action stub for tests
 */
public class MonitoredActionStub extends MonitoredAction implements Cloneable, Serializable {

    private String id;
    private String healthPK;
    private String description;
    private MonitoredAction.State state;
    private Date start;
    private Date finish;
    private long duration;
    private String host;

    /**
     * To make the copy of action
     *
     * @return the copy
     */
    @Override
    public MonitoredAction copy() {
        try {
            return (MonitoredAction) clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * To get the Id (primary key) of monitored action
     *
     * @return the value or null if not saved yet
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * The reference to healthPK
     *
     * @return value of PK
     */
    @Override
    public String getHealthPK() {
        return healthPK;
    }

    /**
     * To get the description of the action
     *
     * @return the value (not null)
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * To get the current state of the action
     *
     * @return the value (not null)
     */
    @Override
    public State getState() {
        return state;
    }

    /**
     * To get the time when action starts
     *
     * @return the value (not null)
     */
    @Override
    public Date getStart() {
        return start;
    }

    /**
     * To get the time when action has finished
     *
     * @return the value (may be null)
     */
    @Override
    public Date getFinish() {
        return finish;
    }

    /**
     * To get the duration of proceeded action
     *
     * @return the value (nanoseconds)
     */
    @Override
    public long getDuration() {
        return duration;
    }

    /**
     * To get the name of host where action proceeded
     *
     * @return the value (not null)
     */
    @Override
    public String getHost() {
        return host;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHealthPK(String healthPK) {
        this.healthPK = healthPK;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void setStart(Date start) {
        this.start = start;
    }

    @Override
    public void setFinish(Date finish) {
        this.finish = finish;
    }

    @Override
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonitoredActionStub that = (MonitoredActionStub) o;
        return getDuration() == that.getDuration() &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(getHealthPK(), that.getHealthPK()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                getState() == that.getState() &&
                Objects.equals(getStart(), that.getStart()) &&
                Objects.equals(getFinish(), that.getFinish()) &&
                Objects.equals(getHost(), that.getHost());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getHealthPK(), getDescription(), getState(), getStart(), getFinish(), getDuration(), getHost());
    }
}
