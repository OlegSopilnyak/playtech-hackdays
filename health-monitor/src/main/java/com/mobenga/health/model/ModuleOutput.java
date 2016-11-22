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
     * @return payload of module's output
     */
    String getPayload();

    /**
     * To create the copy of bean
     *
     * @return copy instance
     */
    ModuleOutput copy();

    /**
     * To assign module's primary key to output
     *
     * @param key key of module
     * @return this
     */
    ModuleOutput setModulePK(String key);

    /**
     * The factory for module's outputs
     */
    interface DeviceFactory{

        /**
         * To create the Device for module's output
         *
         * @param module module-owner of output
         * @return the instance
         */
        Device create(HealthItemPK module);

        /**
         * returns supported type of ModuleOutput
         *
         * @return the value
         */
        String getType();

        /**
         * To check is module ignored for saving
         *
         * @param module module to check
         * @return true if ignored
         */
        boolean isModuleIgnored(HealthItemPK module);
    }

    /**
     * The Device to output module's information
     */
    interface Device {
        /**
         * To create ModuleOutput instance and send it to appropriate storage
         * @param arguments the arguments of output item for payload
         */
        void out(Object... arguments);

        /**
         * Associate monitored action with module's output
         *
         * @param action
         */
        void associate(MonitoredAction action);

        /**
         * To create and associate action
         *
         * @param actionDescription description of action
         */
        void associate(String actionDescription);

        /**
         * To get associated action
         *
         * @return instance or null if no association
         */
        MonitoredAction getAssociated();

        /**
         * To start progress stage of associated action
         */
        void actionBegin();

        /**
         * To finish associated action successfully
         */
        void actionEnd();
        /**
         * To finish associated action with errors
         */
        void actionFail();
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

        /**
         * To test is particular message suitable for the criteria
         *
         * @param message message to test
         * @return true if message is suitable
         */
        boolean isSuitable(ModuleOutput message);
    }
}
