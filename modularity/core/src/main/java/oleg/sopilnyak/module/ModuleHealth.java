/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module;

import oleg.sopilnyak.module.model.ModuleHealthCondition;

/**
 * Type: the health of module
 */
public interface ModuleHealth {
	/**
	 * To get the registry condition of module for the moment
	 *
	 * @return current condition value
	 */
	ModuleHealthCondition getCondition();

	/**
	 * After action detected fail
	 *
	 * @param exception cause of fail
	 */
	void healthGoDown(Throwable exception);

	/**
	 * To get instance of last thrown exception
	 *
	 * @return exception or null if wouldn't
	 */
	Throwable lastThrown();

	/**
	 * After action detected success
	 */
	void healthGoUp();
}
