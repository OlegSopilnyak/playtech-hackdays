/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.FailModuleAction;
import oleg.sopilnyak.module.model.action.ModuleActionRuntimeException;
import oleg.sopilnyak.module.model.action.SuccessModuleAction;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service: Factory of module actions
 *
 * @see oleg.sopilnyak.service.action.ModuleActionFactory
 */
@Slf4j
public class ModuleActionFactoryImpl implements ModuleActionFactory {
    protected final static ThreadLocal<ModuleAction> current = new ThreadLocal<>();

    @Autowired
    private ModuleActionStorage actionsStorage;
    @Autowired
    private ObjectProvider<SuccessModuleAction> successActions;
    @Autowired
    private ObjectProvider<FailModuleAction> failedActions;
    @Autowired
    private ScheduledExecutorService scanRunner;

    @Value("${module.action.storage.delay:200}")
    long delay;

    // the queue of actions to save
    private BlockingQueue<ModuleAction> storageQueue = new LinkedBlockingQueue<>();

    public void setUp() {
        scanRunner.schedule(this::persistScheduledAction, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * To create main action of module
     *
     * @param module owner of action
     * @return instance
     */
    @Override
    public ModuleAction createModuleMainAction(Module module) {
        log.debug("Creating main action for module {}", module.primaryKey());
        final ModuleAction action = actionsStorage.createActionFor(module);
        module.getMetricsContainer().action().changed(action);
        scheduleStorage(action);
        return action;
    }

	/**
     * To create regular module's action
     *
     * @param module owner of action
     * @param name   the name of action
     * @return instance
     */
    @Override
    public ModuleAction createModuleRegularAction(Module module, String name) {
        log.debug("Creating regular '{}' action for module '{}'", name, module.primaryKey());
        final ModuleAction action = actionsStorage.createActionFor(module, current.get(), name);
        module.getMetricsContainer().action().changed(action);
		scheduleStorage(action);

        return action;
    }

    /**
     * Execute in context of module action
     *
     * @param module     owner of simple action
     * @param actionName action's name
     * @param executable runnable to be executed
     * @param rethrow    flag for rethrow exception if occurred
     * @return action-result of execution
     */
    @Override
    public ModuleAction executeAtomicModuleAction(Module module, String actionName, Runnable executable, boolean rethrow) {
        log.debug("Setup action for '{}' of module '{}'", actionName, module.primaryKey());

        final ModuleAction action;
        current.set(action = createModuleRegularAction(module, actionName));

        log.debug("Executing  for action {}", action.getName());

        action.setState(ModuleAction.State.PROGRESS);
        module.getMetricsContainer().action().changed(action);
		scheduleStorage(action);

        actionName = action.getName();
        log.debug("Real name of action is '{}'", actionName);
        try {
            log.debug("Start executing action '{}'", actionName);
            executable.run();
            log.debug("Finished well executing of action '{}'", actionName);
            module.healthGoUp();
        } catch (Throwable t) {
            log.error("Cannot execute action {}", actionName, t);

            module.getMetricsContainer().action().fail(action, t);
            module.healthGoLow(t);
            final ModuleAction result = failedActions.getObject(action, t);
			scheduleStorage(action);

            if (rethrow) {
                throw new ModuleActionRuntimeException(action, "Fail in " + actionName, t);
            }
            return result;
        } finally {
            current.set(action.getParent());
        }

        log.debug("Finished execution of {}", actionName);
        module.getMetricsContainer().action().success(action);
        final ModuleAction result = successActions.getObject(action);
        scheduleStorage(action);

        return result;
    }

    /**
     * To get current action by Thread context
     *
     * @return current action
     */
    @Override
    public ModuleAction currentAction() {
        return current.get();
    }

    /**
     * To start main action for module
     *
     * @param module owner of action
     */
    @Override
    public void startMainAction(Module module) {
        final ModuleAction mainAction = module.getMainAction();
        if (mainAction != current.get()) {
            current.set(mainAction);
        }
        if (mainAction.getState() == ModuleAction.State.PROGRESS) {
            module.getMetricsContainer().action().changed(mainAction);
        }
        scheduleStorage(mainAction);
    }

    /**
     * To finish main action
     *
     * @param module  owner of action
     * @param success flag is it done good
     */
    @Override
    public void finishMainAction(Module module, boolean success) {
        current.set(null);
        log.debug("Main action of '{}' is finished successfully - {}.", module.primaryKey(), success);
        final ModuleAction mainAction = module.getMainAction();
        if (success) {
            successActions.getObject(mainAction);
            module.getMetricsContainer().action().success(mainAction);
        } else {
            failedActions.getObject(mainAction, module.lastThrown());
            module.getMetricsContainer().action().fail(mainAction, module.lastThrown());
        }
		scheduleStorage(mainAction);
    }

    // private method
	private void scheduleStorage(ModuleAction action) {
		log.debug("Schedule to save {}", action);
		storageQueue.offer(new ActionStorageWrapper(action));
	}
	private void persistScheduledAction(){
        final List<ModuleAction> chunk = new ArrayList<>();
        try {
            while (!storageQueue.isEmpty()) {
                chunk.clear();
                final int transferred = storageQueue.drainTo(chunk, 100);
                log.debug("Processing {} actions.", transferred);
                chunk.stream().map(a -> (ActionStorageWrapper) a).forEach(a -> actionsStorage.persist(a));
            }
        }catch (Throwable t){
            log.error("Something went wrong with storage-queue", t);
        }finally {
            log.debug("Scheduling scan for {} millis", delay);
            scanRunner.schedule(this::persistScheduledAction, delay, TimeUnit.MILLISECONDS);
        }
	}
}
