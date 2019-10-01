/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module;

import oleg.sopilnyak.module.model.ModuleAction;

/**
 * Type with values of the module
 */
public interface ModuleValues extends ModuleHealth, ModuleConfigurable {

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
	 * To get reference to main action of module
	 *
	 * @return instance
	 */
	ModuleAction getMainAction();

}
