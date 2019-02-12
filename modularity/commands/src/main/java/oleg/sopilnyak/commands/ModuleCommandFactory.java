/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.commands;

import oleg.sopilnyak.commands.model.ModuleCommandType;

import java.util.Collection;

/**
 * Service: factory of command
 */
public interface ModuleCommandFactory {
	/**
	 * To create new command of appropriate type
	 *
	 * @param type type of command
	 * @return instance
	 */
	ModuleCommand create(ModuleCommandType type);

	/**
	 * To get collection of available commands
	 *
	 * @return commands set of the factory
	 */
	Collection<String> availableCommands();
}
