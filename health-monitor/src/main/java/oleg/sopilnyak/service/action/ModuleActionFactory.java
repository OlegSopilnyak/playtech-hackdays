/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;

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
	 * Execute in context of module action
	 *
	 * @param action     action-context of execution
	 * @param executable runnable to be executed
	 * @param rethrow    flag for rethrow excexption if occurred
	 * @return action-result of execution
	 */
	ModuleAction executeAtomicModuleAction(ModuleAction action, Runnable executable, boolean rethrow);
}
