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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	@DependsOn("getModuleConfigurationService")
	public ChangeConfigurationModuleCommand makeChangeConfigurationModuleCommand() {
		return new ChangeConfigurationModuleCommand();
	}

	/**
	 * Factory of commands
	 *
	 * @return singleton
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	public ModuleCommandFactoryImpl makeModuleCommandFactory() {
		final Map<ModuleCommandType, Class<? extends ModuleCommand>> commandsStore = new ConcurrentHashMap<>();
		commandsStore.put(ModuleCommandType.LIST, ListModuleCommand.class);
		commandsStore.put(ModuleCommandType.STATUS, StatusModuleCommand.class);
		commandsStore.put(ModuleCommandType.START, StartModuleCommand.class);
		commandsStore.put(ModuleCommandType.STOP, StopModuleCommand.class);
		commandsStore.put(ModuleCommandType.RESTART, RestartModuleCommand.class);
		commandsStore.put(ModuleCommandType.CHANGE, ChangeConfigurationModuleCommand.class);
		return new ModuleCommandFactoryImpl(commandsStore);
	}
}
