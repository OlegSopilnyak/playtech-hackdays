package com.mobenga.health.model;

import com.mobenga.health.model.persistence.ValidatingEntity;

import java.util.Date;

/**
 * Simple log message from the module
 */
public abstract class LogMessage implements ModuleOutput, ValidatingEntity{
    // the name of storage item (table/index-type/etc)
    public static final String STORAGE_NAME = "log-message";
    public static final String OUTPUT_TYPE = "log";

    private String id;
    protected String modulePK;
    private String actionId;
    private Date whenOccured;
    private String payload;


    protected LogMessage() {
    }


    /**
     * The unique identifier of the action's output message
     *
     * @return the value (null if it doesn't stored yet)
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * To get pipeline (|) separated module's ID
     * 1.systemId
     * 2.moduleId
     * 3.versionId
     *
     * @return the module-id (not-null)
     */
    @Override
    public String getModulePK() {
        return modulePK;
    }

    /**
     * To get the Id of MonitoredAction witch aggregate module's output
     *
     * @return the value or null if detached output
     */
    @Override
    public String getActionId() {
        return actionId;
    }

    /**
     * To get the type of message (it will be used as discriminator)
     *
     * @return the value (not-null)
     */
    @Override
    public String getMessageType() {
        return OUTPUT_TYPE;
    }

    /**
     * To get the date-time when the action was occurred
     *
     * @return the date-time of event
     */
    @Override
    public Date getWhenOccurred() {
        return whenOccured;
    }

    /**
     * To get the payload of message
     *
     * @return
     */
    @Override
    public String getPayload() {
        return payload;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public void setWhenOccured(Date whenOccured) {
        this.whenOccured = whenOccured;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}
