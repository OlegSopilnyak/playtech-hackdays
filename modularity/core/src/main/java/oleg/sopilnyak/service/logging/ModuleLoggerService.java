/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.logging;

import ch.qos.logback.classic.Level;
import oleg.sopilnyak.service.ServiceModule;

/**
 * Service: service to manage slf4j logging support
 */
public interface ModuleLoggerService extends ServiceModule {
	// The name of properties package
	String PACKAGE = "module.service.logger";
	// The level of severity
	String LEVEL_NAME = "level";
	int LEVEL_DEFAULT = Level.INFO_INT;
	// The pattern for logger output
	String PATTERN_NAME = "layoutPattern";
	String PATTERN_DEFAULT = "[%thread] %-5level %logger{50} - %msg%n";

	/**
	 * Make canonical name of 'level' property
	 *
	 * @return full name
	 */
	default String levelName() {
		return PACKAGE + "." + LEVEL_NAME;
	}

	/**
	 * Make canonical name of 'layoutPattern' property
	 *
	 * @return full name
	 */
	default String patternName() {
		return PACKAGE + "." + PATTERN_NAME;
	}

	/**
	 * To setup severity level of logging
	 *
	 * @param level new value of level
	 */
	void setSeverityLevel(Level level);

	/**
	 * To setup value of layout's pattern
	 *
	 * @param layoutPattern pattern for target message
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
