/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.FailModuleAction;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.module.model.action.ModuleActionExceptionWrapper;
import oleg.sopilnyak.module.model.action.SuccessModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.metric.ActionMetricsContainer;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
	@Autowired
	private TimeService timeService;

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
		action.setName(module.getSystemId() + "-" + module.getModuleId() + ":Main");
		action.setId(idGenerator.generate());
		action.setHostName(hostName);
		action.setDescription("Main action of " + module.getDescription());
		((ActionMetricsContainer) module.getMetricsContainer()).actionChanged(action);
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
		log.debug("Creating regular '{}' action for module {}", name, module.primaryKey());
		final ModuleRegularAction action = new ModuleRegularAction(module, name);
		action.setParent(module.getMainAction());
		action.setId(idGenerator.generate());
		action.setHostName(hostName);
		action.setDescription(name + " action of " + module.getDescription());
		((ActionMetricsContainer) module.getMetricsContainer()).actionChanged(action);
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
		final ModuleAction action;
		current.set(action = createModuleRegularAction(module, actionName));

		log.debug("Executing  for action {}", action.getName());
		final ModuleActionAdapter adapter = (ModuleActionAdapter) action;

		adapter.setState(ModuleAction.State.PROGRESS);
		((ActionMetricsContainer) module.getMetricsContainer()).actionChanged(action);

		try {
			executable.run();
			module.healthGoUp();
		} catch (Throwable t) {
			log.error("Cannot execute action {}", action.getName(), t);

			adapter.setState(ModuleAction.State.FAIL);
			((ActionMetricsContainer) module.getMetricsContainer()).actionFinished(action, t);
			module.healthGoLow(t);

			if (rethrow) {
				throw new ModuleActionExceptionWrapper("Fail in " + action.getName(), t);
			}
			return new FailModuleAction(action, t);
		} finally {
			current.set(action.getParent());
		}

		log.debug("Finished execution of {}", action.getName());
		adapter.setState(ModuleAction.State.SUCCESS);
		((ActionMetricsContainer) module.getMetricsContainer()).actionFinished(action);
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
		current.set(module.getMainAction());
	}
}
