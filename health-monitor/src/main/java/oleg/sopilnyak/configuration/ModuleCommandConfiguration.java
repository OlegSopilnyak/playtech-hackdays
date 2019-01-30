/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.service.control.ModuleCommand;
import oleg.sopilnyak.service.control.impl.ListModuleCommand;
import oleg.sopilnyak.service.control.impl.ModuleCommandFactoryImpl;
import oleg.sopilnyak.service.control.impl.StatusModuleCommand;
import oleg.sopilnyak.service.control.model.ModuleCommandType;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration: configuration for modules system control commands
 */
@Configuration
public class ModuleCommandConfiguration {


	/**
	 * List modules command
	 *
	 * @return prototype
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	@Scope( ConfigurableBeanFactory.SCOPE_PROTOTYPE )
	public ListModuleCommand makeListModuleCommand(){
		return new ListModuleCommand();
	}

	/**
	 * Factory of commands
	 *
	 * @return singleton
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	public ModuleCommandFactoryImpl makeModuleCommandFactory(){
		final Map<ModuleCommandType, Class<? extends ModuleCommand>> commandsStore = new ConcurrentHashMap<>();
		commandsStore.put(ModuleCommandType.LIST, ListModuleCommand.class);
		commandsStore.put(ModuleCommandType.STATUS, StatusModuleCommand.class);
		return new ModuleCommandFactoryImpl(commandsStore);
	}
}
