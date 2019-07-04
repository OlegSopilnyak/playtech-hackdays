/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.service.action.ModuleActionsRepository;
import oleg.sopilnyak.service.configuration.storage.ConfigurationStorageRepository;
import oleg.sopilnyak.service.metric.storage.ModuleMetricsRepository;
import oleg.sopilnyak.service.storage.simpletext.impl.SimpleTextConfigurationRepositoryImpl;
import oleg.sopilnyak.service.storage.simpletext.impl.SimpleTextMetricsRepositoryImpl;
import oleg.sopilnyak.service.storage.simpletext.impl.SimpleTextModuleActionsRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for simple-text-files repositories configuration
 */
@Configuration
public class SimpleTextRepositoryConfiguration {

	@Bean
	public ConfigurationStorageRepository makeConfigurationStorageRepository(){
		return new SimpleTextConfigurationRepositoryImpl();
	}

	@Bean
	public ModuleMetricsRepository makeModuleMetricsRepository(){
		return new SimpleTextMetricsRepositoryImpl();
	}

	@Bean
	public ModuleActionsRepository makeModuleActionsRepository(){
		return new SimpleTextModuleActionsRepositoryImpl();
	}
}
