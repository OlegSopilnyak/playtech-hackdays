/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;

import java.util.concurrent.Callable;

/**
 * Service: Factory to manage module's actions
 */
public interface ModuleActionFactory {
	/**
	 * To create main action of module
	 *
	 * @param module owner of action
	 * @return instance
	 */
	ModuleAction createModuleMainAction(Module module);

	/**
	 * To create regular module's action
	 *
	 * @param module owner of action
	 * @param name the name of action
	 * @return instance
	 */
	ModuleAction createModuleRegularAction(Module module, String name);


	/**
	 * Execute in context of module action using regular action
	 *
	 * @param module owner of simple action
	 * @param actionName action's name
	 * @param executable runnable to be executed
	 * @param rethrow    flag for rethrow exception if occurred
	 * @return action-result of execution
	 */
	ModuleAction executeAtomicModuleAction(Module module, String actionName, Runnable executable, boolean rethrow);

	/**
	 * To create action's context for function
	 *
	 * @param input the value of input parameter
	 * @param function function to be executed
	 * @param <I> type of input parameter
	 * @param <O> type of function's execution
	 * @return instance of action context
	 */
	<I,O> ActionContext<I,O>createContext(I input, Callable<O> function);
	/**
	 * Execute in context of module action using regular action
	 *
	 * @param module owner of simple action
	 * @param actionName action's name
	 * @param context context of atomic action execution
	 * @param rethrow    flag for rethrow exception if occurred
	 * @return action-result of execution
	 */
	ModuleAction executeAtomicModuleAction(Module module, String actionName, ActionContext context, boolean rethrow);

	/**
	 * To create atomic module action instance
	 *
	 * @param module owner of atomic action
	 * @param actionName action's name
	 * @param context context of atomic action execution
	 * @param rethrow    flag for rethrow exception if occurred
	 * @return built instance
	 */
	AtomicModuleAction createAtomicModuleAction(Module module, String actionName, ActionContext context, boolean rethrow);

	/**
	 * To get current action by Thread context
	 *
	 * @return current action
	 */
	ModuleAction currentAction();

	/**
	 * To start main action for module
	 *
	 * @param module owner of the action
	 */
	void startMainAction(Module module);

	/**
	 * To finish main action
	 *
	 * @param module owner of action
	 * @param success flag is it done good
	 */
	void finishMainAction(Module module, boolean success);
}
