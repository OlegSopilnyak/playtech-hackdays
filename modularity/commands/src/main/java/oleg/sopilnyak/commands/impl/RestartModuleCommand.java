/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.commands.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.commands.model.ModuleCommandType;
import oleg.sopilnyak.commands.model.command.SwitchModuleCommandAdapter;
import oleg.sopilnyak.module.Module;
import org.slf4j.Logger;

/**
 * Command: try to stop modules
 */
@Slf4j
public class RestartModuleCommand extends SwitchModuleCommandAdapter {

	/**
	 * To get the type of command
	 *
	 * @return value
	 */
	@Override
	public ModuleCommandType type() {
		return ModuleCommandType.RESTART;
	}

	/**
	 * To get description of command for help
	 *
	 * @return value
	 */
	@Override
	public String description() {
		return "Command to restart module(s).";
	}
// protected methods
	/**
	 * To get command-related logger
	 *
	 * @return logger instance
	 */
	@Override
	protected Logger getLogger() {
		return log;
	}

	/**
	 * To process module and return result message
	 *
	 * @param module to be processed
	 * @return result message
	 */
	@Override
	protected String processModule(Module module) {
		module.restart();
		return  "Restarted well";
	}
}
