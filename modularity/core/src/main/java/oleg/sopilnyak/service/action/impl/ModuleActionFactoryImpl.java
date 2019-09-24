/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.ActionContext;
import oleg.sopilnyak.service.action.AtomicModuleAction;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.action.bean.ActionMapper;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;
import oleg.sopilnyak.service.action.exception.ModuleActionRuntimeException;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service: Factory of module actions
 *
 * @see oleg.sopilnyak.service.action.ModuleActionFactory
 */
@Slf4j
public class ModuleActionFactoryImpl implements ModuleActionFactory {
	final static ThreadLocal<ModuleAction> current = new ThreadLocal<>();

	@Autowired
	ModuleActionStorage actionsStorage;
	@Autowired
	ScheduledExecutorService scanRunner;
	@Autowired
	TimeService timeService;

	@Value("${module.action.storage.delay:200}")
	long delay;

	// the queue of actions to save
	private BlockingQueue<ActionStorageWrapper> storageQueue = new LinkedBlockingQueue<>();

	public void setUp() {
		log.info("Starting scheduling action save from queue.");
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
		if (action instanceof ModuleActionAdapter) {
			((ModuleActionAdapter) action).setStarted(timeService.now());
		}

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
		final Callable<Void> actionCall = () -> {
			executable.run();
			return null;
		};
		final ActionContext<Void, Void> context = createContext(null, actionCall);
		return executeAtomicModuleAction(module, actionName, context, rethrow);
	}

	/**
	 * To create action's context for function
	 *
	 * @param input    the value of input parameter
	 * @param function function to be executed
	 * @return instance of action context
	 */
	@Override
	public <I, O> ActionContext<I, O> createContext(I input, Callable<O> function) {
		return AtomicActionContext.<I, O>builder().input(input).action(function).output(null).criteria(new LinkedHashMap<>()).build();
	}

	/**
	 * Execute in context of module action using regular action
	 *
	 * @param module     owner of simple action
	 * @param actionName action's name
	 * @param context    context of atomic action execution
	 * @param rethrow    flag for rethrow exception if occurred
	 * @return action-result of execution
	 */
	@Override
	public ModuleAction executeAtomicModuleAction(Module module, String actionName, ActionContext context, boolean rethrow) {
		log.debug("Setup action for '{}' of module '{}'", actionName, module.primaryKey());
		final AtomicModuleAction moduleAction = createAtomicModuleAction(module, actionName, context, rethrow);
		return moduleAction.operate();
	}

	/**
	 * To create atomic module action instance
	 *
	 * @param module     owner of atomic action
	 * @param actionName action's name
	 * @param context    context of atomic action execution
	 * @param rethrow    flag for rethrow exception if occurred
	 * @return built instance
	 */
	@Override
	public AtomicModuleAction createAtomicModuleAction(Module module, String actionName, ActionContext context, boolean rethrow) {
		log.debug("Making module's atomic action executor for '{}' of '{}'", module.primaryKey(), actionName);
		return new AtomicActionExecutor(context, actionName, module, rethrow);
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
		log.debug("Starting Main action for '{}'", module.primaryKey());
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
			module.getMetricsContainer().action().success(mainAction);
		} else {
			module.getMetricsContainer().action().fail(mainAction, module.lastThrown());
		}
		scheduleStorage(mainAction);
	}

	// inner classes
	@Data
	@AllArgsConstructor
	final class AtomicActionExecutor implements AtomicModuleAction {
		private ActionContext context;
		private String actionName;
		private Module module;
		private boolean allowedExceptionRethrow;
		private final AtomicBoolean executed = new AtomicBoolean(false);

		/**
		 * To execute action
		 *
		 * @return result of execution
		 */
		public ModuleAction operate() {
			log.debug("Executing action '{}' for module '{}'", actionName, module.primaryKey());

			final ModuleAction action = createModuleRegularAction(module, actionName);
			this.actionName = action.getName();
			// set new regular action as current action
			current.set(action);

			try {
				if (executed.get()) {
					throw new RuntimeException("Atomic action already executed.");
				}
				return executePositiveActionScenario(action);
			} catch (Throwable t) {
				return processOperationErrorModuleAction(action, t);
			} finally {
				finalizeModuleActionOperation(action);
			}
		}

		// executor's private methods
		ModuleAction executePositiveActionScenario(ModuleAction action) throws Exception {
			log.debug("Starting  for action {} context {}", this.actionName, context);
			module.getMetricsContainer().action().start(action, context);

			log.debug("Executing context call for action {}", this.actionName);
			action.setState(ModuleAction.State.PROGRESS);
			module.getMetricsContainer().action().changed(action);
			scheduleStorage(action);

			log.debug("Start executing action '{}'", this.actionName);
			final Object output = context.getAction().call();
			context.saveResult(output);

			module.getMetricsContainer().action().finish(action, output);
			// health is going to be better
			module.healthGoUp();

			log.debug("Finished well execution of '{}' returned: {}", this.actionName, output);
			if (action instanceof ModuleActionAdapter) {
				((ModuleActionAdapter) action).setDuration(timeService.duration(action.getStarted()));
			}
			module.getMetricsContainer().action().success(action);
			scheduleStorage(action);

			return ActionMapper.INSTANCE.toSuccessResult(action);
		}

		ModuleAction processOperationErrorModuleAction(ModuleAction action, Throwable t) {
			log.error("Cannot execute action {}", this.actionName, t);
			// health is going to be worse
			module.healthGoDown(t);

			if (action instanceof ModuleActionAdapter) {
				((ModuleActionAdapter) action).setDuration(timeService.duration(action.getStarted()));
			}
			module.getMetricsContainer().action().fail(action, t);
			scheduleStorage(action);

			if (allowedExceptionRethrow) {
				// throw caught exception
				throw new ModuleActionRuntimeException(action, "Fail in " + this.actionName, t);
			}
			return ActionMapper.INSTANCE.toFailResult(action, t);
		}

		void finalizeModuleActionOperation(ModuleAction action) {
			executed.getAndSet(true);
			// store action's duration metric
			final String label = "atomic-action";
			module.getMetricsContainer().duration()
					.simple(
							label, action, timeService.now(),
							module.primaryKey(), action.getDuration()
					);
			// set as current previous action
			current.set(action.getParent());
		}

	}

	// private method

	void scheduleStorage(ModuleAction action) {
		log.debug("Schedule to save {}", action);
		storageQueue.offer(ActionMapper.INSTANCE.wrap(action, true));
	}

	void persistScheduledAction() {
		final List<ActionStorageWrapper> chunk = new ArrayList<>();
		try {
			while (!storageQueue.isEmpty()) {
				chunk.clear();
				final int transferred = storageQueue.drainTo(chunk, 100);
				log.debug("Processing {} actions.", transferred);
				chunk.forEach(a -> actionsStorage.persist(a));
			}
		} catch (Throwable t) {
			log.error("Something went wrong with storage-queue", t);
		} finally {
			log.debug("Scheduling scan for {} millis", delay);
			scanRunner.schedule(this::persistScheduledAction, delay, TimeUnit.MILLISECONDS);
		}
	}
}
