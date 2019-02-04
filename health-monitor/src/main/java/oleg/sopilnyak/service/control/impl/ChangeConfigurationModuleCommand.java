/**
 * Copyright (C) 2018 - Veristream, LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package oleg.sopilnyak.service.control.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.control.CommandResult;
import oleg.sopilnyak.service.control.model.ModuleCommandType;
import oleg.sopilnyak.service.control.model.ModuleInfoAdapter;
import oleg.sopilnyak.service.control.model.command.ModuleCommandAdapter;
import oleg.sopilnyak.service.control.model.result.CommandResultAdapter;
import oleg.sopilnyak.service.dto.VariableItemDto;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static oleg.sopilnyak.service.control.model.ModuleCommandState.*;
import static oleg.sopilnyak.service.control.model.ModuleCommandType.CHANGE;

@Slf4j
public class ChangeConfigurationModuleCommand extends ModuleCommandAdapter {

	@Autowired
	private ModuleConfigurationStorage storage;

	/**
	 * To get the type of command
	 *
	 * @return value
	 */
	@Override
	public ModuleCommandType type() {
		return CHANGE;
	}

	/**
	 * To process module and transform to ModuleInfo for further displaying
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
				.configuration(module.getConfiguration())
				.build();
	}

	/**
	 * To get Function for main command's activity
	 *
	 * @return Function to apply
	 */
	@Override
	protected Function<Object[], CommandResult> executeCommandApplication() {
		return (parameters) -> executeChange(parameters);
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

	// private methods
	CommandResult executeChange(Object... parameters) {
		final CommandResultAdapter result = makeResult();
		result.setState(PROCESS);
		try {
			final String modulePK = (String) parameters[0];
			final String moduleItemName = (String) parameters[1];
			final String moduleItemValue = (String) parameters[2];
			log.debug("Parameters - module:'{}' item-name:'{}' item-value: '{}'", modulePK, moduleItemName, moduleItemValue);

			final Module module = registry.getRegistered(modulePK);
			final Map<String, VariableItem> configuration = module.getConfiguration();
			final VariableItem item = configuration.get(moduleItemName);
			if (item == null) {
				throw new IllegalArgumentException("No configuration item with name '" + moduleItemName + "' for module '" + modulePK + "'");
			}

			configuration.put(moduleItemName, new VariableItemDto(item, moduleItemValue));
			storage.updateConfiguration(module, configuration);
			result.setState(SUCCESS);
			result.setData(processAndTransform(module));
		} catch (Throwable t) {
			log.error("Cannot change configuration of module.", t);
			result.setState(FAIL);
			result.setData(t);
		}
		return result;
	}

	// inner classes
	@Data
	@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
	static class LongModuleInfo extends ModuleInfoAdapter {
		static final String FORMAT = "Module - %s\nActive - %b\nCondition - %s\n" +
				"Description - %s\n-------\nConfiguration - %s\n====================\n";
		private final Map<String, VariableItem> configuration;

		@Builder
		public LongModuleInfo(String modulePK, boolean active, ModuleHealthCondition condition, String description,
							  Map<String, VariableItem> configuration) {
			super(modulePK, active, condition, description);
			this.configuration = configuration;
		}

		@Override
		public String toTTY() {
			return String.format(FORMAT, modulePK, active, condition, description, configuration).trim();
		}
	}

	class Result extends CommandResultAdapter {
		/**
		 * To get result's data as string for console output
		 *
		 * @return data as tty string
		 */
		@Override
		public String dataAsTTY() {
			final ModuleInfoAdapter module = (ModuleInfoAdapter) data;
			StringBuilder builder = new StringBuilder("Module updated: ")
					.append("\n").append("-------------\n");
			if (Objects.nonNull(module)) {
				builder.append(module.toTTY()).append("\n");
			}
			return builder.toString();
		}

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
