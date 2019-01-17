/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.registry;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;

import java.util.Collection;

/**
 * Service to register-unregister modules, scan registry of module
 */
public interface ModulesRegistry extends Module {
	// The name of properties package
	String PACKAGE = "module.service.heartbeat";
	// HeartBeat delay for Modules Registry
	String DELAY_NAME = "delay";
	int DELAY_DEFAULT = 2000;
	// modules which will ignored during scan
	String IGNORE_MODULE_NAME = "ignoreModule";
	String IGNORE_MODULE_DEFAULT = "";

	/**
	 * Make canonical name of delay property
	 *
	 * @return full name
	 */
	default String delayName() {
		return PACKAGE + "." + DELAY_NAME;
	}

	/**
	 * Make canonical name of ignoreModules property
	 *
	 * @return full name
	 */
	default String ignoreModulesName() {
		return PACKAGE + "." + IGNORE_MODULE_NAME;
	}

	/**
	 * To get the value of module's system
	 *
	 * @return the value
	 */
	@Override
	default String getSystemId() {
		return "ModuleSystem";
	}

	/**
	 * To get the value of module's ID
	 *
	 * @return the value
	 */
	@Override
	default String getModuleId() {
		return "Registry";
	}

	/**
	 * To get the value of module's version
	 *
	 * @return the value
	 */
	@Override
	default String getVersionId() {
		return "0.0.1";
	}

	/**
	 * To get description of module
	 *
	 * @return the value
	 */
	@Override
	default String getDescription() {
		return "The registry of modules.";
	}

	/**
	 * To add module to registry
	 *
	 * @param module to add
	 */
	void add(Module module);

	/**
	 * To remove module from registry
	 *
	 * @param module to remove
	 */
	void remove(Module module);

	/**
	 * To get collection of registered modules
	 *
	 * @return collection of registered modules
	 */
	Collection<Module> registered();

	/**
	 * To get registered module by module's primary key
	 *
	 * @param modulePK primary key
	 * @return module or null if not registered
	 */
	Module getRegistered(String modulePK);
	/**
	 * To get registered module by module instance
	 *
	 * @param module module instance
	 * @return module or null if not registered
	 */
	Module getRegistered(ModuleBasics module);
}
