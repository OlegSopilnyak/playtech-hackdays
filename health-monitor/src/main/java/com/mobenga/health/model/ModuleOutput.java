package com.mobenga.health.model;

import java.util.Date;

/**
 * Logical Model Bean to keep information about module's monitored action output
 */
public interface ModuleOutput {
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
     * @return
     */
    String getPayload();

    /**
     * The factory for module's outputs
     */
    interface DeviceFactory{

        /**
         * To create the Device for module's output
         *
         * @param module
         * @return the instance
         */
        Device create(HealthItemPK module);

        /**
         * returns supported type of ModuleOutput
         *
         * @return the value
         */
        String getType();
    }

    /**
     * The Device to output module's information
     */
    interface Device {
        /**
         * To create ModuleOutput instance and send it to appropriate storage
         * @param actionId the id of action-aggregator (may be null)
         * @param argumments the arguments of output item for payload
         */
        void out(String actionId, Object... argumments);
    }

    /**
     * The criteria of module's ouput selection
     */
    interface Criteria {
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
         * select all entities which happined after the date
         *
         * @return the value or null if ignored
         */
        Date getMoreThan();

        /**
         * select all entities which happend before the date
         *
         * @return the value or null if ignored
         */
        Date getLessThen();
    }
}
