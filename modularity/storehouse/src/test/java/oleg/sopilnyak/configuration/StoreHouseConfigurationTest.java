/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.module.storage.ModuleStorage;
import oleg.sopilnyak.service.action.ModuleActionsRepository;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.configuration.storage.ConfigurationStorageRepository;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.configuration.storage.event.ConfigurationStorageEvent;
import oleg.sopilnyak.service.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.service.metric.storage.ModuleMetricsRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		StoreHouseConfiguration.class
		, StoreHouseConfigurationTest.Config.class
})
public class StoreHouseConfigurationTest {
	@Autowired
	private ApplicationContext context;

	@Test
	public void testGetModuleConfigurationStorage() {
		assertNotNull(context.getBean(ModuleConfigurationStorage.class));
	}
	public void testGeModuleActionStorage() {
		assertNotNull(context.getBean(ModuleActionStorage.class));
	}
	public void testGetModuleMetricStorage() {
		assertNotNull(context.getBean(ModuleMetricStorage.class));
	}

	// inner class-configuration
	@Configuration
	public static class Config {
		@Bean( name = "modules-configuration-map")
		public Map<String, Map<String, VariableItem>> makeSharedCache(){
			return new HashMap<>();
		}

		@Bean( name = "modules-configuration-queue")
		public BlockingQueue<ConfigurationStorageEvent> makeSharedQueue(){
			return new ArrayBlockingQueue<>(100);
		}

		@Bean
		public ConfigurationStorageRepository mockConfigurationStorageRepository(){
			return mock(ConfigurationStorageRepository.class);
		}
		@Bean
		public ModuleActionsRepository mockModuleActionsRepository(){
			return mock(ModuleActionsRepository.class);
		}
		@Bean
		public ModuleMetricsRepository mockModuleMetricsRepository(){
			return mock(ModuleMetricsRepository.class);
		}
		@Bean
		public ModuleStorage makeModuleStorage(){
			return mock(ModuleStorage.class);
		}
	}
}