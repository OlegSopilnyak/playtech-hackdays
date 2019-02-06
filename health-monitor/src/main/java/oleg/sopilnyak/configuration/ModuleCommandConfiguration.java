/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.service.control.ModuleCommand;
import oleg.sopilnyak.service.control.impl.*;
import oleg.sopilnyak.service.control.model.ModuleCommandType;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;

import java.util.LinkedHashMap;
import java.util.Map;

import static oleg.sopilnyak.service.control.model.ModuleCommandType.*;

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
		return new HelpModuleCommand();
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
