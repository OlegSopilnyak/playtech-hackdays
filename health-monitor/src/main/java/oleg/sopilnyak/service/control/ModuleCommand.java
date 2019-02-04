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
	 * To get description of command for help
	 *
	 * @return value
	 */
	String description();

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
	default String name(){
		return type().name().toLowerCase();
	}

	/**
	 * To test is parameters contains 'help'
	 *
	 * @param parameters parameters passed to execute command
	 * @return true if needs command's help
	 */
	default boolean isCommandHelp(Object... parameters){
		return parameters != null && parameters.length == 1 && ("help".equals(parameters[0]) || "?".equals(parameters[0]));
	}

}
