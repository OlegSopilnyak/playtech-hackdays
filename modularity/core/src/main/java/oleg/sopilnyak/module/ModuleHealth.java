/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module;

import oleg.sopilnyak.module.model.ModuleHealthCondition;

/**
 * Type: the health of the module
 */
public interface ModuleHealth {

	/**
	 * Type for control ModuleHealth state
	 */
	interface Control {
		/**
		 * After action detected fail
		 *
		 * @param exception cause of fail
		 */
		void healthGoDown(Throwable exception);


		/**
		 * After action detected success
		 */
		void healthGoUp();

	}

	/**
	 * The current state of module
	 */
	interface State {
		/**
		 * To check is module active (is working)
		 *
		 * @return true if module is working
		 */
		boolean isActive();

		/**
		 * To get the host where module is working
		 *
		 * @return the value
		 */
		String getHost();

		/**
		 * To get instance of last thrown exception
		 *
		 * @return exception or null if wouldn't
		 */
		Throwable lastThrown();

		/**
		 * To get the registry condition of module for the moment
		 *
		 * @return current condition value
		 */
		ModuleHealthCondition getCondition();
	}
}
