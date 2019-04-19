/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.module.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.configuration.storage.event.ConfigurationStorageEvent;
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
@ContextConfiguration(classes = {StoreHouseConfiguration.class, StoreHouseConfigurationTest.Config.class})
public class StoreHouseConfigurationTest {
	@Autowired
	private ApplicationContext context;

	@Test
	public void makeModuleConfigurationStorage() {
		assertNotNull(context.getBean(ModuleConfigurationStorage.class));
	}

	// inner class- configuration
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
		public ModuleConfigurationStorage.Repository makeRepository(){
			return mock(ModuleConfigurationStorage.Repository.class);
		}

		@Bean
		public ModuleMetricStorage makeModuleMetricStorage(){
			return mock(ModuleMetricStorage.class);
		}
	}
}