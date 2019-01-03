/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration;

import oleg.sopilnyak.module.Module;

/**
 * Service: to support modules configuration
 */
public interface ModuleConfigurationService extends Module {
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
		return "Configuration";
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
		return "The service to configure registered modules.";
	}
}
