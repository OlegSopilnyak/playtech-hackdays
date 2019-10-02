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
 * Command: try to start modules
 */
@Slf4j
public class StartModuleCommand extends SwitchModuleCommandAdapter {

	/**
	 * To get the type of command
	 *
	 * @return value
	 */
	@Override
	public ModuleCommandType type() {
		return ModuleCommandType.START;
	}

	/**
	 * To get description of command for help
	 *
	 * @return value
	 */
	@Override
	public String description() {
		return "Command to start module(s).";
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
		if (!module.isWorking()) {
			module.moduleStart();
			return  "Started well";
		} else {
			return  "Already started";
		}
	}
}
