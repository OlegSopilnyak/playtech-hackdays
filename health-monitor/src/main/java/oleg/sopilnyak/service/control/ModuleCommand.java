/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control;

import oleg.sopilnyak.service.control.model.ModuleCommandType;

/**
 * Command: parent of command to communicate with modules registry
 */
public interface ModuleCommand {
	/**
	 * To execute command for registry
	 *
	 * @param parameters parameters for command
	 * @return Execution result
	 */
	CommandResult execute(Object... parameters);

	/**
	 * To get the type of command
	 *
	 * @return value
	 */
	ModuleCommandType type();

	/**
	 * To get the name of command
	 *
	 * @return command's name
	 */
	String name();

	// inner classes

	/**
	 * Types of commands
	 */
}
