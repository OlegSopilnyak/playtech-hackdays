/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.service.control.model.ModuleCommandType;
import oleg.sopilnyak.service.control.model.command.SwitchModuleCommandAdapter;
import org.slf4j.Logger;

import static oleg.sopilnyak.service.control.model.ModuleCommandType.STOP;

/**
 * Command: try to stop modules
 */
@Slf4j
public class StopModuleCommand extends SwitchModuleCommandAdapter {

	/**
	 * To get the type of command
	 *
	 * @return value
	 */
	@Override
	public ModuleCommandType type() {
		return STOP;
	}

	/**
	 * To get description of command for help
	 *
	 * @return value
	 */
	@Override
	public String description() {
		return "Command to stop module(s).";
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
		if (module.isActive()) {
			module.moduleStop();
			return  "Stopped well";
		} else {
			return  "Already stopped";
		}
	}
}
