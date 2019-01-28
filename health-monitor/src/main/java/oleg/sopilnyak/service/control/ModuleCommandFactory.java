/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control;

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
	ModuleCommand create(ModuleCommand.Type type);
}
