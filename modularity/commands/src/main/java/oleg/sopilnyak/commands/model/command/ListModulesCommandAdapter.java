/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.commands.model.command;

import oleg.sopilnyak.commands.CommandResult;
import oleg.sopilnyak.commands.model.ModuleCommandState;
import oleg.sopilnyak.commands.model.ModuleInfoAdapter;
import oleg.sopilnyak.commands.model.result.CommandResultAdapter;
import oleg.sopilnyak.service.ServiceModule;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
		result.setState(ModuleCommandState.PROCESS);
		try {
			log.debug("Getting list of modules with parameters '{}'", Arrays.asList(parameters));
			final List<ModuleInfoAdapter> modules = registry.registered().stream()
					.filter(m -> isEnabled(m.primaryKey(), parameters))
					.map(module -> processAndTransform((ServiceModule) module))
					.collect(Collectors.toList());
			result.setData(modules);
			log.debug("Selected {} modules.", modules.size());
			result.setState(ModuleCommandState.SUCCESS);
		} catch (Throwable t) {
			log.error("Cannot get list of modules.", t);
			result.setState(ModuleCommandState.FAIL);
			result.setData(t);
		}
		return result;
	}

}
