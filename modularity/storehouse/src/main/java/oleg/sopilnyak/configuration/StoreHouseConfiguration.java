/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.service.action.impl.ModuleActionStorageImpl;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.configuration.storage.impl.ModuleConfigurationStorageImpl;
import oleg.sopilnyak.service.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.service.metric.storage.impl.ModuleMetricStorageImpl;
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

	/**
	 * The storage of module-actions
	 *
	 * @return singleton
	 */
	@Bean(name = "module.actionStorage", autowire = Autowire.BY_TYPE, initMethod = "setUp")
	public ModuleActionStorageImpl makeModuleActionStorage(){
		return new ModuleActionStorageImpl();
	}

	/**
	 * The storage of modules' metrics
	 *
	 * @return singleton
	 */
	@Bean(name = "module.metricStorage", autowire = Autowire.BY_TYPE)
	public ModuleMetricStorage makeModuleMetricStorage(){
		return new ModuleMetricStorageImpl();
	}
}
