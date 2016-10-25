package com.mobenga.health.model;

import com.mobenga.health.model.persistence.ValidatingEntity;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Entity-bean to store in Elasticsearch index
 */
public final class MonitoredActionEntity extends MonitoredAction implements ValidatingEntity, Cloneable, StringEntity {
    private static final SimpleDateFormat dateConverter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    // the PK of entity
    private String id;

    // foreign key to HealthItemPK entity
    private String healthPK;

    // The description of monitored action
    private String description;

    // Current phase of action
    private String stateName;

    // Date-time when action starts
    private Date start;

    // Date-time when action finish (be any reason)
    private Date finish;

    // The duration of action (nano-seconds)
    private long duration;

    // The name of host where action proceeded
    private String host;

    @Override
    public String toString() {
        return id +"#"
                + healthPK + "#"
                + description + "#"
                + stateName + "#"
                + dateConverter.format(start) + "#"
                + (finish == null ? " -none- " : dateConverter.format(finish)) + "#"
                + duration + "#"
                + host
                ;
    }

    @Override
    public StringEntity fromString(String value) {
        MonitoredActionEntity entity = new MonitoredActionEntity();
        StringTokenizer st = new StringTokenizer(value, "#");
        entity.setId(st.nextToken());
        entity.setHealthPK(st.nextToken());
        entity.setDescription(st.nextToken());
        entity.setStateName(st.nextToken());
        try {
            entity.setStart(dateConverter.parse(st.nextToken()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            entity.setFinish(dateConverter.parse(st.nextToken()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        entity.setDuration(Integer.parseInt(st.nextToken()));
        entity.setHost(st.nextToken());
        return entity;
    }

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
            throw new EntityInvalidState("clone()", "Can't make clone.");
        }
    }

    /**
     * To validate internal state of entity if state invalid throws EntityInvalidState
     */
    @Override
    public void validate() {
        if (StringUtils.isEmpty(healthPK)) {
            throw new EntityInvalidState("healthPK", "The reference to HealthItemPK is empty");
        }
        if (StringUtils.isEmpty(host)) {
            throw new EntityInvalidState("host", "The host name is empty");
        }
        if (StringUtils.isEmpty(description)) {
            throw new EntityInvalidState("description", "The description is empty");
        }
        final State state;
        if ((state = getState()) == null) {
            throw new EntityInvalidState("state", "The state is null");
        }
        switch (state) {
            case INIT:
                break;

            case FAIL:
            case SUCCESS:
                if (finish == null) {
                    throw new EntityInvalidState("finish", "The date of finish is null");
                }
                if (duration <= 0) {
                    throw new EntityInvalidState("duration", "The date of finish is null");
                }
                break;

            case PROGRESS:
                if (start == null) {
                    throw new EntityInvalidState("start", "The date of start is null");
                }
                break;
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

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getHealthPK() {
        return healthPK;
    }

    public void setHealthPK(String healthPK) {
        this.healthPK = healthPK;
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
        return StringUtils.isEmpty(stateName) ? null : State.valueOf(stateName);
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

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * To update the description value
     *
     * @param description new value
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * To update the time when action starts
     *
     * @param starts new value
     */
    @Override
    public void setStart(Date starts) {
        this.start = starts;
    }

    /**
     * To update the current state of the action
     *
     * @param state new value
     */
    @Override
    public void setState(State state) {
        this.stateName = state.name();
    }

    /**
     * To update the time when action has finished
     *
     * @param ends new value
     */
    @Override
    public void setFinish(Date ends) {
        this.finish = ends;
    }

    /**
     * To update the duration of proceeded action
     *
     * @param duration new value (nanoseconds)
     */
    @Override
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

}
