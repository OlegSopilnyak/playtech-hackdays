/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.logging;

import ch.qos.logback.classic.Level;
import oleg.sopilnyak.module.Module;

/**
 * Service: service to manage slf4j logging support
 */
public interface ModuleLoggerService extends Module {
	/**
	 * To setup severity level of logging
	 *
	 * @param level new value of level
	 */
	void serSeverityLevel(Level level);

	/**
	 * To setup value of layout's pattern
	 *
	 * @param layoutPattern
	 */
	void setLayoutPattern(String layoutPattern);
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
		return "Logger";
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
		return "The service to interrupt log";
	}
}
