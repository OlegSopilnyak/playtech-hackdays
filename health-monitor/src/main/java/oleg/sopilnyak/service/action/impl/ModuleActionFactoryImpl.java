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
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Service: Factory of module actions
 * @see oleg.sopilnyak.service.action.ModuleActionFactory
 */
@Slf4j
public class ModuleActionFactoryImpl implements ModuleActionFactory {
	private String hostName;
	protected final static ThreadLocal<ModuleAction> current = new ThreadLocal<>();

	@Autowired
	private UniqueIdGenerator idGenerator;

	@PostConstruct
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
		action.setId(idGenerator.generate());
		action.setHostName(hostName);
		action.setDescription("Main action of " + module.getDescription());
		module.getMetricsContainer().actionChanged(action);
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
		action.setId(idGenerator.generate());
		action.setHostName(hostName);
		action.setDescription(name + " action of " + module.getDescription());
		module.getMetricsContainer().actionChanged(action);
		return action;
	}

	/**
	 * Execute in context of module action
	 *
	 * @param action     action-context of execution
	 * @param executable runnable to be executed
	 * @param rethrow    flag for rethrow exception when it occurred
	 * @return action-result of execution
	 */
	@Override
	public ModuleAction executeAtomicModuleAction(ModuleAction action, Runnable executable, boolean rethrow) {
		log.debug("Executing  for action {}", action.getName());
		final ModuleActionAdapter adapter = (ModuleActionAdapter) action;
		final Module currentModule = (Module) action.getModule();

		adapter.setState(ModuleAction.State.PROGRESS);
		currentModule.getMetricsContainer().actionChanged(action);

		try {
			executable.run();
			currentModule.healthGoUp();
		} catch (Throwable t) {
			log.error("Cannot execute action {}", action.getName(), t);

			adapter.setState(ModuleAction.State.FAIL);
			currentModule.getMetricsContainer().actionFinished(action, t);
			currentModule.healthGoLow();

			if (rethrow) {
				throw new ModuleActionExceptionWrapper("Fail in " + action.getName(), t);
			}
			return new FailModuleAction(action, t);
		}

		log.debug("Finished execution of {}", action.getName());
		adapter.setState(ModuleAction.State.SUCCESS);
		currentModule.getMetricsContainer().actionFinished(action);
		return new SuccessModuleAction(action);
	}
}
