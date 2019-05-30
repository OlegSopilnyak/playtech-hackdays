/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.DurationMetricsContainer;
import oleg.sopilnyak.module.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.service.action.impl.ModuleActionFactoryImpl;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.configuration.storage.impl.ModuleConfigurationServiceImpl;
import oleg.sopilnyak.service.logging.impl.ModuleSlf4jLogAppender;
import oleg.sopilnyak.service.metric.impl.MetricsContainerImpl;
import oleg.sopilnyak.service.metric.impl.ModuleMetricsConfiguration;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import oleg.sopilnyak.service.registry.impl.HealthModuleRegistryServiceImpl;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

/**
 * Configuration: main configuration for modules system
 */
@Configuration
@Import({
		ModuleUtilityConfiguration.class,
		ModuleActionsConfiguration.class,
		ModuleMetricsConfiguration.class
})
public class ModuleSystemConfiguration {

	/**
	 * Service: factory of metrics
	 *
	 * @return prototype
	 * @see oleg.sopilnyak.module.metric.MetricsContainer
	 * @see ActionMetricsContainer
	 * @see HeartBeatMetricContainer
	 * @see DurationMetricsContainer
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public MetricsContainerImpl getMetricsContainer() {
		return new MetricsContainerImpl();
	}

	/**
	 * Service: factory of actions
	 *
	 * @return singleton
	 * @see oleg.sopilnyak.service.action.ModuleActionFactory
	 */
	@Bean(autowire = Autowire.BY_TYPE, initMethod = "setUp")
	public ModuleActionFactoryImpl getModuleActionFactory() {
		return new ModuleActionFactoryImpl();
	}

	/**
	 * Storage: engine to manage actions persistence
	 *
	 * @return singleton
	 * @see ModuleActionStorage
	 */
//	@Bean(autowire = Autowire.BY_TYPE, initMethod = "setUp")
//	public ModuleActionStorageImpl makeModuleActionStorage(){
//		return new ModuleActionStorageImpl();
//	}

	/**
	 * Service: service to serve change configuration in registered modules
	 *
	 * @return singleton
	 * @see oleg.sopilnyak.service.configuration.ModuleConfigurationService
	 */
	@Bean(name = "module.configurationService", autowire = Autowire.BY_TYPE, initMethod = "moduleStart", destroyMethod = "moduleStop")
	public ModuleConfigurationServiceImpl getModuleConfigurationService() {
		return new ModuleConfigurationServiceImpl();
	}

	/**
	 * Service: service for modules health check, and registry
	 *
	 * @return singleton
	 * @see ModulesRegistryService
	 */
	@Bean(name = "module.healthCheckService", autowire = Autowire.BY_TYPE, initMethod = "moduleStart", destroyMethod = "moduleStop")
	public HealthModuleRegistryServiceImpl getHealthModuleService() {
		return new HealthModuleRegistryServiceImpl();
	}

	/**
	 * Service: module for Slf4j appender
	 *
	 * @return singleton
	 * @see oleg.sopilnyak.service.logging.ModuleLoggerService
	 */
	@Bean(name = "module.slf4LoggerService", autowire = Autowire.BY_TYPE, initMethod = "moduleStart", destroyMethod = "moduleStop")
	public ModuleSlf4jLogAppender getModuleLogAppender() {
		return new ModuleSlf4jLogAppender();
	}
}
