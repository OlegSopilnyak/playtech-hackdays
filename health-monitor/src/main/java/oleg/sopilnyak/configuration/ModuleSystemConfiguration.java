/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.service.action.impl.ModuleActionFactoryImpl;
import oleg.sopilnyak.service.configuration.impl.ModuleConfigurationServiceImpl;
import oleg.sopilnyak.service.logging.ModuleLogAppender;
import oleg.sopilnyak.service.metric.impl.MetricsContainerImpl;
import oleg.sopilnyak.service.registry.impl.HealthModuleService;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration: main configuration for modules system
 */
@Configuration
@Import({ModuleUtilityConfiguration.class})
public class ModuleSystemConfiguration {

	/**
	 * Service: factory of metrics
	 * @see oleg.sopilnyak.module.metric.MetricsContainer
	 * @see oleg.sopilnyak.service.metric.ActionMetricsContainer
	 * @see oleg.sopilnyak.service.metric.HeartBeatMetricContainer
	 *
	 * @return singleton
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	public MetricsContainerImpl getMetricsContainer(){
		return new MetricsContainerImpl();
	}
	/**
	 * Service: factory of actions
	 * @see oleg.sopilnyak.service.action.ModuleActionFactory
	 *
	 * @return singleton
	 */
	@Bean(autowire = Autowire.BY_TYPE, initMethod = "setUp")
	public ModuleActionFactoryImpl getModuleActionFactory(){
		return new ModuleActionFactoryImpl();
	}

	/**
	 * Service: service to serve change configuration in registered modules
	 * @see oleg.sopilnyak.service.configuration.ModuleConfigurationService
	 *
	 * @return singleton
	 */
	@Bean(autowire = Autowire.BY_TYPE, initMethod = "initialSetUp", destroyMethod = "shutdownModule")
	public ModuleConfigurationServiceImpl getModuleConfigurationService(){
		return new ModuleConfigurationServiceImpl();
	}

	/**
	 * Service: service for modules health check, and registry
	 * @see oleg.sopilnyak.service.registry.ModulesRegistry
	 *
	 * @return singleton
	 */
	@Bean(autowire = Autowire.BY_TYPE, initMethod = "initialSetUp", destroyMethod = "shutdownModule")
	public HealthModuleService getHealthModuleService(){
		return new HealthModuleService();
	}

	@Bean(autowire = Autowire.BY_TYPE, initMethod = "registerAppender", destroyMethod = "unRegisterAppender")
	public ModuleLogAppender getModuleLogAppender(){
		return new ModuleLogAppender();
	}
}
