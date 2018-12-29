/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service;

import oleg.sopilnyak.module.Module;

/**
 * Service to register-unregister modules
 */
public interface ModulesRegistry extends Module {
	/**
	 * To get the value of module's system
	 *
	 * @return the value
	 */
	@Override
	default String getSystemId(){
		return "Modules_System";
	}

	/**
	 * To get the value of module's ID
	 *
	 * @return the value
	 */
	@Override
	default String getModuleId(){
		return "Registry";
	}

	/**
	 * To get the value of module's version
	 *
	 * @return the value
	 */
	@Override
	default String getVersionId(){
		return "0.0.1";
	}

	/**
	 * To get description of module
	 *
	 * @return the value
	 */
	@Override
	default String getDescription(){
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
}
