/**
 * Copyright (C) 2018 - Veristream, LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package oleg.sopilnyak.service.control.model;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.service.control.CommandResult;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static oleg.sopilnyak.service.control.model.ModuleCommandState.*;

/**
 * Command: parent of commands for list of modules
 */
public abstract class ListModulesCommandAdapter extends AbstractModuleCommand {
	/**
	 * To execute command for registry
	 *
	 * @param parameters parameters for command
	 * @return Execution result
	 */
	@Override
	public final CommandResult execute(Object... parameters) {
		return executeList(getLogger(), parameters);
	}

	/**
	 * To make command result instance
	 *
	 * @return instance
	 */
	protected abstract AbstractCommandResult makeResult();

	/**
	 * To get command-related logger
	 *
	 * @return logger instance
	 */
	protected abstract Logger getLogger();

	/**
	 * To process module and transform to ModuleInfo for further display
	 *
	 * @param module to process by command and transform to info
	 * @return module to info transformation
	 */
	protected abstract ModuleInfo processAndTransform(Module module);

	// private methods
	private CommandResult executeList(final Logger log, final Object... parameters) {
		AbstractCommandResult result = makeResult();
		result.setState(PROCESS);
		try {
			log.debug("Getting list of modules {}", Arrays.asList(parameters));
			final List<ModuleInfo> modules = registry.registered().stream()
					.filter(m -> isEnabled(m.primaryKey(), parameters))
					.map(m -> processAndTransform(m))
					.collect(Collectors.toList());
			result.setData(modules);
			log.debug("Selected {} modules.", modules.size());
			result.setState(SUCCESS);
		} catch (Throwable t) {
			log.error("Cannot get list of modules.", t);
			result.setState(FAIL);
			result.setData(t);
		}
		return result;
	}
}
