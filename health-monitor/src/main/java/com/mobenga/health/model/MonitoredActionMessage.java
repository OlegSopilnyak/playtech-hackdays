package com.mobenga.health.model;

import java.util.Date;

/**
 * Logical Model Bean to keep information about monitored action output
 */
public interface MonitoredActionMessage {
    /**
     * The unique identifier of the action's output message
     *
     * @return the value
     */
    String getId();

    /**
     * To get the Id of joined MonitoredAction
     *
     * @return the value or null if joined to session
     */
    String getActionId();

    /**
     * To get pipeline (|) separated location of module
     * 1.systemId
     * 2.moduleId
     * 3.versionId
     * 4.host
     *
     * @return the location
     */
    String getModuleLocation();

    /**
     * To get the type of message (it will be used as discriminator)
     *
     * @return the value
     */
    String getMessageType();

    /**
     * To get the date-time when the action was occurred
     *
     * @return the date-time of event
     */
    Date getWhenOccurred();

    /**
     * To get the payload of message
     *
     * @return
     */
    String getPayload();
}
