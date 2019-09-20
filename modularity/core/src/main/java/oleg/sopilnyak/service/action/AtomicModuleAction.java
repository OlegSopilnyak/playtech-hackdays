/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;

/**
 * Type: interface to container for atomic module action
 */
public interface AtomicModuleAction {

	/**
	 * To execute action
	 *
	 * @return result of execution
	 */
	ModuleAction operate();

	/**
	 * To get action's execution context
	 *
	 * @return execution context
	 */
	ActionContext getContext();

	/**
	 * To get the name of atomic action
	 *
	 * @return the value
	 */
	String getActionName();
	/**
	 * The module-owner of atomic action
	 *
	 * @return reference to module
	 */
	Module getModule();

	/**
	 * If during action execution occurred some exception situation would exception be rethrowing after execution.
	 *
	 * @return rethrow allowed if true
	 */
	boolean isAllowedExceptionRethrow();
}
