/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import oleg.sopilnyak.service.control.CommandResult;
import oleg.sopilnyak.service.control.ModuleCommand;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Command: abstract parent of module command to communicate with modules registry
 */
public abstract class AbstractModuleCommand implements ModuleCommand {

	@Autowired
	protected ModulesRegistry registry;
	@Autowired
	protected ObjectMapper jsonMapper;
	/**
	 * To execute command for registry
	 *
	 * @param parameters parameters for command
	 * @return Execution result
	 */
	@Override
	public CommandResult execute(Object... parameters) {
		throw new IllegalStateException("Not realized here.");
	}

}
