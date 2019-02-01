/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.model;

import lombok.Builder;
import lombok.Data;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleHealthCondition;

import java.util.List;
import java.util.Objects;

/**
 * Command: try to start modules
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
		return (parameters == null || parameters.length != 1) ? false : modulePK.startsWith((String) parameters[0]);
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
	 * To process module and transform to ModuleInfo for further display
	 *
	 * @param module to process by command and transform to info
	 * @return module to info transformation
	 */
	@Override
	protected ModuleInfo processAndTransform(Module module) {
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
	static class ShortModuleInfo extends ModuleInfo {
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

	@Data
	class Result extends AbstractCommandResult {
		/**
		 * To get result's data as string for console output
		 *
		 * @return data as tty string
		 */
		@Override
		public String dataAsTTY() {
			final List<ModuleInfo> modules = (List<ModuleInfo>) data;
			StringBuilder builder = new StringBuilder("Modules selected: ")
					.append(modules == null ? 0 : modules.size())
					.append("\n").append("-------------\n");
			if (Objects.nonNull(modules)) {
				modules.forEach(info -> builder.append(info.toTTY()).append("\n"));
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
