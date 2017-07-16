package com.mobenga.health.model.business.out.log;

import com.mobenga.health.model.business.out.ModuleOutputMessage;
import com.mobenga.health.model.persistence.ValidatingEntity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple log message from the module
 */
public abstract class ModuleLoggerMessage implements ModuleOutputMessage, ValidatingEntity{
    // format of date-time applied for log-message
    public static final SimpleDateFormat DATE_TIME_FORMATER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    // the name of storage item (table/index-type/etc)
    public static final String STORAGE_NAME = "log-message";
    public static final String LOG_OUTPUT_TYPE = "log";

    private String id;
    protected String modulePK;
    private String actionId;
    private Date whenOccurred;
    private String payload;


    protected ModuleLoggerMessage() {
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
        return LOG_OUTPUT_TYPE;
    }

    /**
     * To get the date-time when the action was occurred
     *
     * @return the date-time of event
     */
    @Override
    public Date getWhenOccurred() {
        return whenOccurred;
    }

    /**
     * To get the payload of message
     *
     * @return the payload of message
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

    public void setWhenOccured(Date whenOccurred) {
        this.whenOccurred = whenOccurred;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}
