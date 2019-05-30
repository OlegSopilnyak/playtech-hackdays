/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.module.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.module.metric.storage.impl.ModuleMetricStorageImpl;
import oleg.sopilnyak.service.action.impl.ModuleActionStorageImpl;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.configuration.storage.impl.ModuleConfigurationStorageImpl;
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
		return new ModuleConfigurationStorageImpl();
	}

	@Bean(name = "module.actionStorage", autowire = Autowire.BY_TYPE, initMethod = "setUp")
	public ModuleActionStorage makeModuleActionStorage(){
		return new ModuleActionStorageImpl();
	}

	@Bean(name = "module.metricStorage", autowire = Autowire.BY_TYPE)
	public ModuleMetricStorage makeModuleMetricStorage(){
		return new ModuleMetricStorageImpl();
	}
}
