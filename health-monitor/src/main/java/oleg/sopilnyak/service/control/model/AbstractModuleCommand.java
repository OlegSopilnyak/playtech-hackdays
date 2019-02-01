/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import oleg.sopilnyak.service.control.CommandResult;
import oleg.sopilnyak.service.control.ModuleCommand;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

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

	/**
	 * To get the name of command
	 *
	 * @return command's name
	 */
	@Override
	public String name() {
		return type().name().toLowerCase();
	}

	/**
	 * To check is module enabled for processing
	 *
	 * @param modulePK   primary-key of module
	 * @param parameters array of suffixes
	 * @return true if primary-key starts with one of suffixes
	 */
	protected boolean isEnabled(String modulePK, Object[] parameters) {
		if (parameters == null || parameters.length == 0) {
			return true;
		}
		return Stream.of(parameters)
				.map(p -> (String) p)
				.filter(prefix -> !"*".equals(prefix))
				.anyMatch(prefix -> modulePK.toLowerCase().startsWith(prefix.toLowerCase()))
				;
	}

}
