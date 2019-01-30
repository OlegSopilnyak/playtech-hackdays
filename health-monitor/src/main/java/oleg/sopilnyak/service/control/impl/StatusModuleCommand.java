/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.control.CommandResult;
import oleg.sopilnyak.service.control.model.AbstractCommandResult;
import oleg.sopilnyak.service.control.model.AbstractModuleCommand;
import oleg.sopilnyak.service.control.model.ModuleCommandType;
import oleg.sopilnyak.service.control.model.ModuleInfo;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static oleg.sopilnyak.service.control.model.ModuleCommandType.STATUS;

/**
 * Command: get status of modules
 */
@Slf4j
public class StatusModuleCommand extends AbstractModuleCommand {
	/**
	 * To execute command for registry
	 *
	 * @param parameters parameters for command
	 * @return Execution result
	 */
	@Override
	public CommandResult execute(Object... parameters) {
		Result result = new Result();
		return result;
	}

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

	// private methods

	// inner classes
	static class LongModuleInfo extends ModuleInfo {
		@Builder
		public LongModuleInfo(String modulePK, boolean active, ModuleHealthCondition condition, String description, ModuleActionInfo mainAction) {
			super(modulePK, active, condition, description);
			this.mainAction = mainAction;
		}

		static final String FORMAT = "Module - %s\nActive - %b\nCondition - %s\nMain:Action - %s\nDescription - %s\n";
		private ModuleActionInfo mainAction;

		@Override
		public String toTTY(){
			return String.format(FORMAT, modulePK, active, condition, mainAction.toTTY(), description).trim();
		}
	}

	@Data
	@AllArgsConstructor
	@Builder
	static class ModuleActionInfo {
		static final String FORMAT = "%s Host: %b Started: %s Duration: %s State: %s";
		private String name;
		private String host;
		private Instant started;
		private long duration;
		private ModuleAction.State state;

		String toTTY() {
			return String.format(FORMAT, name, host, started, duration, state).trim();
		}
	}

	@Data
	class Result extends AbstractCommandResult {
		/**
		 * To get result's data as string for console output
		 *
		 * @return data as tty string
		 */
		@Override
		public String dataAsTTY() {
			StringBuilder builder = new StringBuilder("-------------\n");
			if (Objects.nonNull(data)) {
				((List<ModuleInfo>) data).forEach(info -> builder.append(info.toTTY()).append("\n"));
			}
			return builder.toString();
		}

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
