package com.mobenga.health.monitor;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.ModuleOutput;
import com.mobenga.health.model.ModulePK;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.model.transport.ConfiguredVariableItemDto;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Logger facility for monitored modules
 */
public interface ModuleLoggerDeviceFactory extends ModuleOutput.DeviceFactory, MonitoredService {

    // names of configured parameter "ignoreModules"
    String PARAMS_PACKAGE = "health.monitor.service.module.output.log";
    String IGNORE_MODULES_NAME = "ignoreModules";
    ConfiguredVariableItem IGNORE_MODULES
            = new ConfiguredVariableItemDto(IGNORE_MODULES_NAME, "The set of modules to ignore logging for.", "none");
    // canonical name of configured parameter
    String IGNORE_MODULES_FULL_NAME = PARAMS_PACKAGE + "." + IGNORE_MODULES_NAME;

    /**
     * to get the value of item's system
     *
     * @return the value
     */
    @Override
    default public String getSystemId() {
        return "healthMonitor";
    }

    /**
     * to get the value of item's application
     *
     * @return the value
     */
    @Override
    default public String getApplicationId() {
        return "modulesOuputService";
    }

    /**
     * to get the value of item's application version
     *
     * @return the value
     */
    @Override
    default public String getVersionId() {
        return "0.01";
    }

    /**
     * to get description of module
     *
     * @return the value
     */
    @Override
    default public String getDescription() {
        return "Module for log-messages";
    }

    /**
     * Class-logger device to manage log output in context of current monitored
     * action
     */
    static abstract class AbstractLogger implements ModuleOutput.Device {

        protected final ModulePK module;
        protected MonitoredAction action;
        private final Lock actionLock = new ReentrantLock();

        public AbstractLogger(ModulePK module) {
            this.module = module;
        }

        /**
         * To create ModuleOutput instance and send it to appropriate storage
         *
         * @param arguments the arguments of output item for payload
         */
        @Override
        public void out(Object... arguments) {
            asyncOutput(module, action, arguments);
        }

        /**
         * Associate monitored newAction with module's output
         *
         * @param newAction newAction which become the main newAction of logger
         */
        @Override
        public void associate(MonitoredAction newAction) {
            try {
                actionLock.lock();
                if (!isActive()) {
                    return;
                }

                if (newAction != null) {
                    asyncInitMonitoredAction(module, newAction);
                }
                this.action = newAction;
            } finally {
                actionLock.unlock();
            }
        }

        /**
         * To create and associate action
         *
         * @param actionDescription description of action
         */
        @Override
        public void associate(String actionDescription) {
            try {
                actionLock.lock();
                if (!isActive()) {
                    return;
                }

                final MonitoredAction newAction = createMonitoredAction();
                newAction.setDescription(actionDescription);
                asyncInitMonitoredAction(module, newAction);
                this.action = newAction;
            } finally {
                actionLock.unlock();
            }
        }

        /**
         * To get associated action
         *
         * @return instance or null if no associated action
         */
        @Override
        public MonitoredAction getAssociated() {
            try {
                actionLock.lock();
                return action;
            } finally {
                actionLock.unlock();
            }
        }

        /**
         * To start progress stage of associated action
         */
        @Override
        public void actionBegin() {
            if (!isActive()) {
                return;
            }

            if (Objects.isNull(action)) {
                return;
            }
            try {
                actionLock.lock();
                asyncUpdateMonitoredAction(module, action, MonitoredAction.State.PROGRESS);
            } finally {
                actionLock.unlock();
            }

        }

        /**
         * To finish associated action successfully
         */
        @Override
        public void actionEnd() {
            if (!isActive()) {
                return;
            }

            if (Objects.isNull(action)) {
                return;
            }
            try {
                actionLock.lock();
                asyncUpdateMonitoredAction(module, action, MonitoredAction.State.SUCCESS);
            } finally {
                actionLock.unlock();
            }

        }

        /**
         * To finish associated action with errors
         */
        @Override
        public void actionFail() {
            if (!isActive()) {
                return;
            }

            if (Objects.isNull(action)) {
                return;
            }
            try {
                actionLock.lock();
                asyncUpdateMonitoredAction(module, action, MonitoredAction.State.FAIL);
            } finally {
                actionLock.unlock();
            }

        }

        /**
         * To check is logger active
         *
         * @return true if active
         */
        public abstract boolean isActive();

        /**
         * Asynchronous output of logger
         *
         * @param module module-source of output
         * @param action action-context of output
         * @param arguments output message
         */
        protected abstract void asyncOutput(ModulePK module, MonitoredAction action, Object... arguments);

        /**
         * To create new instance of monitored action
         *
         * @return new instance
         */
        protected abstract MonitoredAction createMonitoredAction();

        /**
         * Asynchronous start action for logger
         *
         * @param module target of logger
         * @param action monitored action associated with logger
         */
        protected abstract void asyncInitMonitoredAction(ModulePK module, MonitoredAction action);

        /**
         * To update the state of monitored action for module
         *
         * @param module module reporter
         * @param action current action
         * @param state new state of monitored action
         */
        protected abstract void asyncUpdateMonitoredAction(ModulePK module, MonitoredAction action, MonitoredAction.State state);

    }
}
