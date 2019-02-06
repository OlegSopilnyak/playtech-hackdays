/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.control.model.ModuleCommandType;
import oleg.sopilnyak.service.control.model.ModuleInfoAdapter;
import oleg.sopilnyak.service.control.model.command.ListModulesCommandAdapter;
import oleg.sopilnyak.service.control.model.result.CommandResultAdapter;
import oleg.sopilnyak.service.control.model.result.ListModulesCommandResultAdapter;
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
	 * To get description of command for help
	 *
	 * @return value
	 */
	@Override
	public String description() {
		return "Command to get broad status of module(s).";
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
	@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
	static class LongModuleInfo extends ModuleInfoAdapter {
		static final String FORMAT = "Module - %s\nActive - %b\nCondition - %s\n" +
				"Main:Action - %s\nDescription - %s\n-------\nConfiguration - %s\n====================\n";
		private final ModuleActionInfo mainAction;
		private final Map<String, VariableItem> configuration;

		@Builder
		public LongModuleInfo(String modulePK, boolean active, ModuleHealthCondition condition, String description,
							  ModuleActionInfo mainAction, Map<String, VariableItem> configuration) {
			super(modulePK, active, condition, description);
			this.mainAction = mainAction;
			this.configuration = configuration;
		}

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
