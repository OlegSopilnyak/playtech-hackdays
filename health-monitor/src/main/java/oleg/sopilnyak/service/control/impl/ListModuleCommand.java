/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.control.CommandResult;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Command: print list of modules information
 */
@Slf4j
public class ListModuleCommand extends AbstractModuleCommand {
	/**
	 * To execute command for registry
	 *
	 * @param parameters parameters for command
	 * @return Execution result
	 */
	@Override
	public CommandResult execute(Object... parameters) {
		Result result = new Result();
		result.setState(CommandResult.State.PROCESS);
		try {
			final List<ModuleInfo> modules = registry.registered().stream()
					.map(m -> toInfo(m))
					.filter(info ->isEnabled(info.getModulePK(), parameters))
					.collect(Collectors.toList());
			result.setData(modules);
			result.setState(CommandResult.State.SUCCESS);
		} catch (Throwable t) {
			result.setState(CommandResult.State.FAIL);
			result.setData(t);
		}
		return result;
	}


	/**
	 * To get the type of command
	 *
	 * @return value
	 */
	@Override
	public Type type() {
		return Type.LIST;
	}

	/**
	 * To get the name of command
	 *
	 * @return command's name
	 */
	@Override
	public String name() {
		return "list";
	}

	// private methods
	static boolean isEnabled(String modulePK, Object[] parameters) {
		// todo check is modulePK correlated with parameters
		return true;
	}
	static ModuleInfo toInfo(Module module){
		return ModuleInfo.builder()
				.modulePK(module.primaryKey())
				.active(module.isActive())
				.condition(module.getCondition())
				.description(module.getDescription())
				.build();
	}

	// inner classes
	@Data
	@AllArgsConstructor
	@Builder
	static class ModuleInfo{
		static final String FORMAT = "%-25s Active: %-5b Condition: %-10s Description: %-20.20s";
		private String modulePK;
		private boolean active;
		private ModuleHealthCondition condition;
		private String description;

		@Override
		public String toString() {
			return String.format(FORMAT, modulePK, active, condition, description).trim();
		}
	}
	@Data
	class Result implements CommandResult {
		public Result() {
			state = State.INIT;
		}

		private State state;
		private Object data;

		/**
		 * To get result's data as string for console output
		 *
		 * @return data as tty string
		 */
		@Override
		public String dataAsTTY() {
			StringBuilder builder = new StringBuilder("Modules:\n").append("-------------\n");
			if (Objects.nonNull(data)) {
				((List<ModuleInfo>) data).forEach(info -> builder.append(info).append("\n"));
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
				return "{\"status\": \"failed :" + t.getMessage() + "\"}";
			}
		}
	}
}
