package com.mobenga.health.model.transport;

import com.mobenga.health.model.business.MonitoredAction;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Transport object for monitored action
 */
public class MonitoredActionDto implements MonitoredAction,Cloneable, Serializable {

    private static final long serialVersionUID = 7126799335911846251L;
    private String id;
    private String host;
    private String moduleKey;
    private String description;
    private State state;
    private Date start;
    private Date finish;
    private long duration;
    /**
     * To make the copy of action
     *
     * @return the copy
     */
    @Override
    public MonitoredAction copy() {
        try {
            return (MonitoredActionDto) super.clone();
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
     * The ModuleKey as string
     *
     * @return string value of module key
     */
    @Override
    public String getModuleKey() {
        return moduleKey;
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

    public void setHost(String host) {
        this.host = host;
    }

    public void setModuleKey(String moduleKey) {
        this.moduleKey = moduleKey;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setFinish(Date finish) {
        this.finish = finish;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonitoredActionDto that = (MonitoredActionDto) o;
        return getDuration() == that.getDuration() &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(getHost(), that.getHost()) &&
                Objects.equals(getModuleKey(), that.getModuleKey()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                getState() == that.getState() &&
                Objects.equals(getStart(), that.getStart()) &&
                Objects.equals(getFinish(), that.getFinish());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getHost(), getModuleKey(), getDescription(), getState(), getStart(), getFinish(), getDuration());
    }

    @Override
    public String toString() {
        return "MonitoredActionDto{" +
                "id='" + id + '\'' +
                ", host='" + host + '\'' +
                ", moduleKey='" + moduleKey + '\'' +
                ", description='" + description + '\'' +
                ", state=" + state +
                ", start=" + start +
                ", finish=" + finish +
                ", duration=" + duration +
                '}';
    }
}
