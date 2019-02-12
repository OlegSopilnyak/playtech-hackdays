/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.commands.model.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import oleg.sopilnyak.commands.model.ModuleInfoAdapter;
import oleg.sopilnyak.commands.model.result.CommandResultAdapter;
import oleg.sopilnyak.commands.model.result.ListModulesCommandResultAdapter;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleHealthCondition;

/**
 * Command: parent of any commands which try to switch modules
 */
public abstract class SwitchModuleCommandAdapter extends ListModulesCommandAdapter {

	// protected methods

	/**
	 * To check is module enabled for processing
	 *
	 * @param modulePK   primary-key of module
	 * @param parameters array of suffixes
	 * @return true if primary-key starts with one of suffixes
	 */
	@Override
	protected boolean isEnabled(String modulePK, Object[] parameters) {
		return !(parameters == null || parameters.length != 1) && modulePK.startsWith((String) parameters[0]);
	}

	/**
	 * To make command result instance
	 *
	 * @return instance
	 */
	@Override
	protected CommandResultAdapter makeResult() {
		return new Result();
	}


	/**
	 * To process module and transform to ModuleInfo for further display
	 *
	 * @param module to process by command and transform to info
	 * @return module to info transformation
	 */
	@Override
	protected ModuleInfoAdapter processAndTransform(Module module) {
		final String message = processModule(module);
		return ShortModuleInfo.builder()
				.modulePK(module.primaryKey())
				.active(module.isActive())
				.condition(module.getCondition())
				.description(message)
				.build();
	}

	/**
	 * To process module and return result message
	 *
	 * @param module to be processed
	 * @return result message
	 */
	protected abstract String processModule(Module module);

	// inner classes
	static class ShortModuleInfo extends ModuleInfoAdapter {
		static final String FORMAT = "%-25s Active: %-5b Condition: %-10s Description: %-20.20s";

		@Builder
		public ShortModuleInfo(String modulePK, boolean active, ModuleHealthCondition condition, String description) {
			super(modulePK, active, condition, description);
		}

		@Override
		public String toTTY() {
			return String.format(FORMAT, modulePK, active, condition, description).trim();
		}
	}

	class Result extends ListModulesCommandResultAdapter {
		/**
		 * To get access to external JSON mapper
		 *
		 * @return instance
		 */
		@Override
		protected ObjectMapper getMapper() {
			return jsonMapper;
		}
	}
}
