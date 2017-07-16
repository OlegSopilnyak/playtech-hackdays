package com.mobenga.health.model.business.out.log;

import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.MonitoredAction;
import com.mobenga.health.model.business.out.ModuleOutputDevice;
import com.mobenga.health.model.transport.MonitoredActionDto;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class-logger device to manage log output in context of current monitored
 * action
 */
public abstract class ModuleLoggerDevice implements ModuleOutputDevice {

    protected final ModuleKey module;
    protected MonitoredAction action;
    private final Lock actionLock = new ReentrantLock();

    public ModuleLoggerDevice(ModuleKey module) {
        this.module = module;
    }

    /**
     * To create ModuleOutputMessage instance and send it to appropriate storage
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
            ((MonitoredActionDto)newAction).setDescription(actionDescription);
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
    protected abstract void asyncOutput(ModuleKey module, MonitoredAction action, Object... arguments);

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
    protected abstract void asyncInitMonitoredAction(ModuleKey module, MonitoredAction action);

    /**
     * To update the state of monitored action for module
     *
     * @param module module reporter
     * @param action current action
     * @param state new state of monitored action
     */
    protected abstract void asyncUpdateMonitoredAction(ModuleKey module, MonitoredAction action, MonitoredAction.State state);

}
