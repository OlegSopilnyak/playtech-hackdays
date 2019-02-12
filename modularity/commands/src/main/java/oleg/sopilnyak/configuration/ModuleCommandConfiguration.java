/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.commands.ModuleCommand;
import oleg.sopilnyak.commands.impl.*;
import oleg.sopilnyak.commands.model.ModuleCommandType;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;

import java.util.LinkedHashMap;
import java.util.Map;

import static oleg.sopilnyak.commands.model.ModuleCommandType.*;

/**
 * Configuration: configuration for modules system control commands
 */
@Configuration
public class ModuleCommandConfiguration {
	/**
	 * To list modules command
	 *
	 * @return prototype
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public ListModuleCommand makeListModuleCommand() {
		return new ListModuleCommand();
	}

	/**
	 * To get status of modules command
	 *
	 * @return prototype
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StatusModuleCommand makeStatusModuleCommand() {
		return new StatusModuleCommand();
	}

	/**
	 * To start modules command
	 *
	 * @return prototype
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StartModuleCommand makeStartModuleCommand() {
		return new StartModuleCommand();
	}

	/**
	 * To stop modules command
	 *
	 * @return prototype
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StopModuleCommand makeStopModuleCommand() {
		return new StopModuleCommand();
	}

	/**
	 * To restart modules command
	 *
	 * @return prototype
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public RestartModuleCommand makeRestartModuleCommand() {
		return new RestartModuleCommand();
	}

	/**
	 * To change module's configuration command
	 *
	 * @return prototype
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	@DependsOn("module.configurationService")
	public ChangeConfigurationModuleCommand makeChangeConfigurationModuleCommand() {
		return new ChangeConfigurationModuleCommand();
	}

	/**
	 * To change module's configuration command
	 *
	 * @return prototype
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	@DependsOn("makeModuleCommandFactory")
	public HelpModuleCommand makeHelpModuleCommand() {
		final Map<ModuleCommandType, String[]> detailedHelp = new LinkedHashMap<>();
		detailedHelp.put(LIST, new String[]{
				"[prefix1] [prefix2]"
				, "prefix1 and prefix2 are prefixes of modules which"
				, "will included to command's result."
				, "if no options, all modules will be shown."
		});
		detailedHelp.put(STATUS, new String[]{
				"[prefix1] [prefix2]"
				, "prefix1 and prefix2 are prefixes of modules which"
				, "will included to command's result."
				, "if no options, all modules will be shown."
		});
		detailedHelp.put(CHANGE, new String[]{
				"<module> <item-name> <item-value>"
				, "module: module's primaryKey value"
				, "item-name: the name of item in module's configuration"
				, "item-value: new value item in module's configuration"
		});
		detailedHelp.put(START, new String[]{
				"[prefix1] [prefix2]"
				, "prefix1 and prefix2 are prefixes of modules which"
				, "will included to command's result."
				, "if no options, all modules will be started."
		});
		detailedHelp.put(STOP, new String[]{
				"[prefix1] [prefix2]"
				, "prefix1 and prefix2 are prefixes of modules which"
				, "will included to command's result."
				, "if no options, all modules will be stopped."
		});
		detailedHelp.put(RESTART, new String[]{
				"[prefix1] [prefix2]"
				, "prefix1 and prefix2 are prefixes of modules which"
				, "will included to command's result."
				, "if no options, all modules will be restarted."
		});
		detailedHelp.put(HELP, new String[]{
				"[command]"
				, "command: if we need help by command usage."
				, "in case of no options, the list of commands will be returned."
		});


		return new HelpModuleCommand(detailedHelp);
	}

	/**
	 * Factory of commands
	 *
	 * @return singleton
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	public ModuleCommandFactoryImpl makeModuleCommandFactory() {
		final Map<ModuleCommandType, Class<? extends ModuleCommand>> commandsStore = new LinkedHashMap<>();
		commandsStore.put(LIST, ListModuleCommand.class);
		commandsStore.put(STATUS, StatusModuleCommand.class);
		commandsStore.put(CHANGE, ChangeConfigurationModuleCommand.class);
		commandsStore.put(START, StartModuleCommand.class);
		commandsStore.put(STOP, StopModuleCommand.class);
		commandsStore.put(RESTART, RestartModuleCommand.class);
		commandsStore.put(HELP, HelpModuleCommand.class);
		return new ModuleCommandFactoryImpl(commandsStore);
	}
}
