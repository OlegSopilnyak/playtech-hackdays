/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.module.metric.storage.ModuleMetricsRepository;
import oleg.sopilnyak.service.action.ModuleActionsRepository;
import oleg.sopilnyak.service.configuration.storage.ConfigurationStorageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		SimpleTextRepositoryConfiguration.class
})
public class SimpleTextRepositoryConfigurationTest {
	@Autowired
	private ApplicationContext context;

	@Test
	public void makeConfigurationStorageRepository() {
		assertNotNull(context.getBean(ConfigurationStorageRepository.class));
	}

	@Test
	public void makeModuleMetricsRepository() {
		assertNotNull(context.getBean(ModuleMetricsRepository.class));
	}

	@Test
	public void makeModuleActionsRepository() {
		assertNotNull(context.getBean(ModuleActionsRepository.class));
	}
}