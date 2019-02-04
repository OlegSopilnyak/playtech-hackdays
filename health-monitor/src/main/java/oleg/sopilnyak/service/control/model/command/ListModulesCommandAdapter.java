/**
 * Copyright (C) 2018 - Veristream, LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package oleg.sopilnyak.service.control.model.command;

import oleg.sopilnyak.service.control.CommandResult;
import oleg.sopilnyak.service.control.model.ModuleInfoAdapter;
import oleg.sopilnyak.service.control.model.result.CommandResultAdapter;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static oleg.sopilnyak.service.control.model.ModuleCommandState.*;

/**
 * Command: parent of commands for list of modules
 */
public abstract class ListModulesCommandAdapter extends ModuleCommandAdapter {
	/**
	 * To get Function for main command's activity
	 *
	 * @return Function to apply
	 */
	@Override
	protected Function<Object[], CommandResult> executeCommandApplication() {
		return (parameters) -> executeList(getLogger(), parameters);
	}

	// private methods
	CommandResult executeList(final Logger log, final Object... parameters) {
		final CommandResultAdapter result = makeResult();
		result.setState(PROCESS);
		try {
			log.debug("Getting list of modules with parameters '{}'", Arrays.asList(parameters));
			final List<ModuleInfoAdapter> modules = registry.registered().stream()
					.filter(m -> isEnabled(m.primaryKey(), parameters))
					.map(module -> processAndTransform(module))
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
