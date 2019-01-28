/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import lombok.Data;
import oleg.sopilnyak.service.control.CommandResult;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
			final List<String> modules = registry.registered().stream()
					.map(m -> m.primaryKey())
					.filter(pk->isEnabled(pk, parameters))
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
	private boolean isEnabled(String modulePK, Object[] parameters) {
		// todo check is modulePK correlated with parameters
		return true;
	}

	// inner classes
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
				((List<String>) data).forEach(pk -> builder.append(pk).append("\n"));
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
				return jsonMapper.writeValueAsString(data);
			} catch (Throwable t) {
				return "{\"status\": \"failed :" + t.getMessage() + "\"}";
			}
		}
	}
}
