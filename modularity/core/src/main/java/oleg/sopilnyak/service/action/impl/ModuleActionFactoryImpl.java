/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.ActionContext;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.action.bean.ActionMapper;
import oleg.sopilnyak.service.action.exception.ModuleActionRuntimeException;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;

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
	private ScheduledExecutorService scanRunner;
	@Autowired
	private TimeService timeService;

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
		final Callable actionCall = new Callable() {
			@Override
			public Object call() throws Exception {
				executable.run();
				return null;
			}
		};
		final ActionContext context = AtomicActionContext.builder().action(actionCall).criteria(new LinkedHashMap<>()).build();
		return executeAtomicModuleAction(module, actionName, context, rethrow);
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

		final Instant startMark = timeService.now();
		final ModuleAction action = createModuleRegularAction(module, actionName);
		final String realActionName = action.getName();
		// set new regular action as current action
		current.set(action);

		log.debug("Starting  for action {} context {}", realActionName, context);
		module.getMetricsContainer().action().start(action, context);

		log.debug("Executing context call for action {}", realActionName);
		action.setState(ModuleAction.State.PROGRESS);
		module.getMetricsContainer().action().changed(action);
		scheduleStorage(action);

		try {
			log.debug("Start executing action '{}'", realActionName);

			final Object output = context.getAction().call();

			module.getMetricsContainer().action().finish(action, output);
			// health is going to be better
			module.healthGoUp();

			log.debug("Finished well execution of {} returned {}", realActionName, output);
			module.getMetricsContainer().action().success(action);
			scheduleStorage(action);

			return ActionMapper.INSTANCE.toSuccessResult(action);
		} catch (Throwable t) {
			log.error("Cannot execute action {}", realActionName, t);
			// health is going to be worse
			module.healthGoLow(t);

			module.getMetricsContainer().action().fail(action, t);
			scheduleStorage(action);

			if (rethrow) {
				// throw caught exception
				throw new ModuleActionRuntimeException(action, "Fail in " + realActionName, t);
			}
			return ActionMapper.INSTANCE.toFailResult(action, t);
		} finally {
			// store action's duration metric
			final Instant nowMark = timeService.now();
			final long duration = timeService.duration(startMark);
			final String modulePK = module.primaryKey();
			final String label = "atomic-action";

			module.getMetricsContainer().duration().simple(label, action, nowMark, modulePK, duration);
			// set as current previous action
			current.set(action.getParent());
		}
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
			module.getMetricsContainer().action().success(mainAction);
		} else {
			module.getMetricsContainer().action().fail(mainAction, module.lastThrown());
		}
		scheduleStorage(mainAction);
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
