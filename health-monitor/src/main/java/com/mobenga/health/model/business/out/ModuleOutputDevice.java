package com.mobenga.health.model.business.out;

import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.MonitoredAction;

/**
 * The Device to output module's information
 */
public interface ModuleOutputDevice {
    /**
     * To create ModuleOutputMessage instance and send it to appropriate storage
     * @param arguments the arguments of output item for payload
     */
    void out(Object... arguments);

    /**
     * Associate monitored action with module's output
     *
     * @param action action associated with output
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

    /**
     * Factory for module output devices
     */
    interface Factory {

        /**
         * To create the Device for module's output
         *
         * @param module module-owner of output
         * @return the instance
         */
        ModuleOutputDevice create(ModuleKey module);

        /**
         * returns supported type of ModuleOutputMessage
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
        boolean isModuleIgnored(ModuleKey module);

    }
}
