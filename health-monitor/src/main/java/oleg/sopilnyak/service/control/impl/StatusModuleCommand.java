/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.control.model.*;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.Map;

import static oleg.sopilnyak.service.control.model.ModuleCommandType.STATUS;

/**
 * Command: get status of modules
 */
@Slf4j
public class StatusModuleCommand extends ListModulesCommandAdapter {
	/**
	 * To get the type of command
	 *
	 * @return value
	 */
	@Override
	public ModuleCommandType type() {
		return STATUS;
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
		return LongModuleInfo.builder()
				.modulePK(module.primaryKey())
				.active(module.isActive())
				.condition(module.getCondition())
				.description(module.getDescription())
				.mainAction(toInfo(module.getMainAction()))
				.configuration(module.getConfiguration())
				.build();
	}

	// private methods
	ModuleActionInfo toInfo(ModuleAction action){
		return ModuleActionInfo.builder()
				.name(action.getName())
				.host(action.getHostName())
				.started(action.getStarted())
				.duration(action.getDuration())
				.state(action.getState())
				.build();
	}
	// inner classes
	@Data
	static class LongModuleInfo extends ModuleInfo {
		@Builder
		public LongModuleInfo(String modulePK, boolean active, ModuleHealthCondition condition, String description,
							  ModuleActionInfo mainAction, Map<String, VariableItem> configuration) {
			super(modulePK, active, condition, description);
			this.mainAction = mainAction;
			this.configuration = configuration;
		}

		static final String FORMAT = "Module - %s\nActive - %b\nCondition - %s\n" +
				"Main:Action - %s\nDescription - %s\n-------\nConfiguration - %s\n====================\n";
		private ModuleActionInfo mainAction;
		private Map<String, VariableItem> configuration;

		@Override
		public String toTTY() {
			return String.format(FORMAT, modulePK, active, condition, mainAction.toTTY(), description, configuration).trim();
		}
	}

	@Data
	@AllArgsConstructor
	@Builder
	static class ModuleActionInfo {
		static final String FORMAT = "%s Host: %s Started: %s Duration: %s State: %s";
		private String name;
		private String host;
		private Instant started;
		private long duration;
		private ModuleAction.State state;

		String toTTY() {
			return String.format(FORMAT, name, host, started, duration, state).trim();
		}
	}

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
