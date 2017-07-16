package com.mobenga.health.model.business.out;

import java.util.Date;

/**
 * Logical Model Bean to keep information about module's monitored action output
 */
public interface ModuleOutputMessage {
    /**
     * The unique identifier of the action's output message
     *
     * @return the value (null if it doesn't stored yet)
     */
    String getId();

    /**
     * To get pipeline (|) separated module's ID
     * 1.systemId
     * 2.moduleId
     * 3.versionId
     *
     * @return the module-id (not-null)
     */
    String getModulePK();

    /**
     * To get the Id of MonitoredAction witch aggregate module's output
     *
     * @return the value or null if detached output
     */
    String getActionId();

    /**
     * To get the type of message (it will be used as discriminator)
     *
     * @return the value (not-null)
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
     * @return payload of module's output
     */
    String getPayload();

    /**
     * To create the copy of bean
     *
     * @return copy instance
     */
    ModuleOutputMessage copy();

    /**
     * To assign module's primary key to output
     *
     * @param key key of module
     * @return this
     */
    ModuleOutputMessage setModulePK(String key);

}
