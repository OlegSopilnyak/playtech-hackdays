/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.control.model.ModuleCommandType;
import oleg.sopilnyak.service.control.model.ModuleInfoAdapter;
import oleg.sopilnyak.service.control.model.command.ListModulesCommandAdapter;
import oleg.sopilnyak.service.control.model.result.CommandResultAdapter;
import oleg.sopilnyak.service.control.model.result.ListModulesCommandResultAdapter;
import org.slf4j.Logger;

import static oleg.sopilnyak.service.control.model.ModuleCommandType.LIST;

/**
 * Command: get list of modules information
 */
@Slf4j
public class ListModuleCommand extends ListModulesCommandAdapter {
	/**
	 * To get the type of command
	 *
	 * @return value
	 */
	@Override
	public ModuleCommandType type() {
		return LIST;
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
	 * To get command-related logger
	 *
	 * @return logger instance
	 */
	@Override
	protected Logger getLogger() {
		return log;
	}

	/**
	 * To process module and transform to ModuleInfo for further display
	 *
	 * @param module to process by command and transform to info
	 * @return module to info transformation
	 */
	@Override
	protected ModuleInfoAdapter processAndTransform(Module module) {
		return ShortModuleInfo.builder()
				.modulePK(module.primaryKey())
				.active(module.isActive())
				.condition(module.getCondition())
				.description(module.getDescription())
				.build();
	}

	// private methods
	// inner classes
	static class ShortModuleInfo extends ModuleInfoAdapter {
		static final String FORMAT = "%-25s Active: %-5b Condition: %-10s Description: %-20.20s";
		@Builder
		public ShortModuleInfo(String modulePK, boolean active, ModuleHealthCondition condition, String description) {
			super(modulePK, active, condition, description);
		}

		@Override
		public String toTTY(){
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
