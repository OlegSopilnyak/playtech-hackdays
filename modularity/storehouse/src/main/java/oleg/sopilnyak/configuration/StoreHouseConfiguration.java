/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.configuration.storage.impl.ModuleConfigurationRepositoryStorageImpl;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration for Store House
 */
@Configuration
@Import(ModuleSystemConfiguration.class)
public class StoreHouseConfiguration {
	/**
	 * The storage of modules' configurations
	 *
	 * @return singleton
	 */
	@Bean(name = "module.configurationStorage", autowire = Autowire.BY_TYPE)
	public ModuleConfigurationStorage makeModuleConfigurationStorage(){
		return new ModuleConfigurationRepositoryStorageImpl();
	}
}
