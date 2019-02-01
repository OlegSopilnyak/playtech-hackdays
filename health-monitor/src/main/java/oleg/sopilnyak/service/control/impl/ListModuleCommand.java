/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.control.model.*;
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
	 * To get the name of command
	 *
	 * @return command's name
	 */
	@Override
	public String name() {
		return type().name().toLowerCase();
	}

	/**
	 * To make command result instance
	 *
	 * @return instance
	 */
	@Override
	protected AbstractCommandResult makeResult() {
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
	protected ModuleInfo processAndTransform(Module module) {
		return ShortModuleInfo.builder()
				.modulePK(module.primaryKey())
				.active(module.isActive())
				.condition(module.getCondition())
				.description(module.getDescription())
				.build();
	}

	// private methods
	// inner classes
	static class ShortModuleInfo extends ModuleInfo {
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

	@Data
	class Result extends ListModulesCommandResultAdapter {

		/**
		 * To get result's data as string for JS communication
		 *
		 * @return data as json string
		 */
		@Override
		public String dataAsJSON() {
			try {
				return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
			} catch (Throwable t) {
				return "{\"status\": \"failed :" + t.getClass().getSimpleName() + " - " + t.getMessage() + "\"}";
			}
		}
	}
}
