/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.commands.model.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import oleg.sopilnyak.commands.CommandResult;
import oleg.sopilnyak.commands.ModuleCommand;
import oleg.sopilnyak.commands.impl.HelpModuleCommand;
import oleg.sopilnyak.commands.model.ModuleInfoAdapter;
import oleg.sopilnyak.commands.model.result.CommandResultAdapter;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Command: abstract parent of module command to communicate with modules registry
 */
public abstract class ModuleCommandAdapter implements ModuleCommand {

	@Autowired
	protected ModulesRegistry registry;
	@Autowired
	protected ObjectMapper jsonMapper;
	@Autowired
	private ObjectFactory<HelpModuleCommand> helpCommandFactory;

	/**
	 * To execute command for registry
	 *
	 * @param parameters parameters for command
	 * @return Execution result
	 */
	@Override
	public CommandResult execute(Object... parameters) {
		return isCommandHelp(parameters) ? describeTheCommand() : executeCommandApplication().apply(parameters);
	}

	/**
	 * To get description of command for help
	 *
	 * @return value
	 */
	@Override
	public String description() {
		return "Usage of " + name() + " is simple";
	}

	// protected methods

	/**
	 * To process module and transform to ModuleInfo for further displaying
	 *
	 * @param module to process by command and transform to info
	 * @return module to info transformation
	 */
	protected abstract ModuleInfoAdapter processAndTransform(Module module);

	/**
	 * To get Function for main command's activity
	 *
	 * @return Function to apply
	 */
	protected abstract Function<Object[], CommandResult> executeCommandApplication();

	/**
	 * To make command result instance
	 *
	 * @return instance
	 */
	protected abstract CommandResultAdapter makeResult();

	/**
	 * To get command-related logger
	 *
	 * @return logger instance
	 */
	protected abstract Logger getLogger();

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

	/**
	 * To describe the command using description
	 *
	 * @return rules of command's usage
	 * @see #description()
	 */
	protected CommandResult describeTheCommand() {
		return helpCommandFactory.getObject().execute(name());
	}

}
