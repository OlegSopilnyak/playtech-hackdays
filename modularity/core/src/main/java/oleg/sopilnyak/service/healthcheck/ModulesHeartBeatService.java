/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.healthcheck;

import oleg.sopilnyak.module.Module;

/**
 * Service to check the health of registered modules
 */
public interface ModulesHeartBeatService extends Module {
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
		return "HearBeat";
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
		return "The health monitor of registered modules.";
	}
}
