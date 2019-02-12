/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.FailModuleAction;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.module.model.action.ModuleActionRuntimeException;
import oleg.sopilnyak.module.model.action.SuccessModuleAction;
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Service: Factory of module actions
 *
 * @see oleg.sopilnyak.service.action.ModuleActionFactory
 */
@Slf4j
public class ModuleActionFactoryImpl implements ModuleActionFactory {
	private String hostName;
	protected final static ThreadLocal<ModuleAction> current = new ThreadLocal<>();

	@Autowired
	private UniqueIdGenerator idGenerator;

	public void setUp() {
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.warn("Cannot get network properties.");
			hostName = "localhost";
		}
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
		final ModuleMainAction action = new ModuleMainAction(module);
		action.setName("[main->" + module.getSystemId() + "->" + module.getModuleId() + "]");
		action.setId(idGenerator.generate());
		action.setHostName(hostName);
		action.setDescription("Main action of " + module.getDescription());
		module.getMetricsContainer().action().changed(action);
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
		final ModuleRegularAction action = new ModuleRegularAction(module, name);
		final ModuleAction parent = current.get();
		action.setParent(Objects.isNull(parent) ? module.getMainAction() : parent);
		action.setId(idGenerator.generate());
		action.setHostName(hostName);
		action.setDescription(name + " action of " + module.getDescription());
		module.getMetricsContainer().action().changed(action);
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
		final ModuleActionAdapter action;
		current.set(action = (ModuleActionAdapter) createModuleRegularAction(module, actionName));

		log.debug("Executing  for action {}", action.getName());

		action.setState(ModuleAction.State.PROGRESS);
		module.getMetricsContainer().action().changed(action);

		try {
			executable.run();
			module.healthGoUp();
		} catch (Throwable t) {
			log.error("Cannot execute action {}", action.getName(), t);

			module.getMetricsContainer().action().fail(action, t);
			module.healthGoLow(t);

			if (rethrow) {
				throw new ModuleActionRuntimeException(action, "Fail in " + action.getName(), t);
			}
			return new FailModuleAction(action, t);
		} finally {
			current.set(action.getParent());
		}

		log.debug("Finished execution of {}", action.getName());
		module.getMetricsContainer().action().success(action);
		return new SuccessModuleAction(action);
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
		final ModuleAction mainAction = module.getMainAction();
		if (success) {
			module.getMetricsContainer().action().success(mainAction);
		} else {
			module.getMetricsContainer().action().fail(mainAction, module.lastThrown());
		}
	}
}
