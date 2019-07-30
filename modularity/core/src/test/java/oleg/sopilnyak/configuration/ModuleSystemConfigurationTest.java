/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.DurationMetricsContainer;
import oleg.sopilnyak.module.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.action.storage.ModuleActionStorageStub;
import oleg.sopilnyak.service.configuration.ModuleConfigurationService;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.service.metric.storage.SelectCriteria;
import oleg.sopilnyak.service.metric.storage.StoredMetric;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		ModuleSystemConfiguration.class
		, ModuleSystemConfigurationTest.Config.class
})
public class ModuleSystemConfigurationTest {
	@Autowired
	private ApplicationContext context;

	@Test
	public void testGetMetricsContainer() {
		assertNotNull(context.getBean(MetricsContainer.class));
		assertNotNull(context.getBean(ActionMetricsContainer.class));
		assertNotNull(context.getBean(HeartBeatMetricContainer.class));
		assertNotNull(context.getBean(DurationMetricsContainer.class));
	}

	@Test
	public void testGetModuleActionFactory() {
		assertNotNull(context.getBean(ModuleActionFactory.class));
	}

	@Test
	public void testGetModuleConfigurationService() {
		assertNotNull(context.getBean(ModuleConfigurationService.class));
	}

	@Test
	public void testGetHealthModuleService() {
		assertNotNull(context.getBean(ModulesRegistryService.class));
	}

	@Test
	public void testDuring5seconds() throws InterruptedException {
		TimeUnit.SECONDS.sleep(5);
	}

	// inner class- configuration
	@Configuration
	public static class Config {

		@Bean
		public ModuleActionStorage stubModuleActionStorage(){
			return new ModuleActionStorageStub();
		}
		@Bean
		public ModuleMetricStorage getModuleMetricStorage() {
			ModuleMetricStorage storage = new ModuleMetricStorage() {
				/**
				 * To store the metric
				 *
				 * @param name           name of metric
				 * @param module         PK of module-owner
				 * @param measured       time when metric was measured
				 * @param host           the host where module is running
				 * @param metricAsString value of metric
				 */
				@Override
				public void storeMetric(String name, String module, Instant measured, String host, String actionId, String metricAsString) {
					System.out.print("- metric ->");
					System.out.print(" name: " + name);
					System.out.print(" module: " + module);
					System.out.print(" action-id: " + actionId);
					System.out.print(" measured: " + measured);
					System.out.print(" host: " + host);
					System.out.println(" message: " + metricAsString);
				}

				/**
				 * To find metrics by criteria
				 *
				 * @param criteria select metrics criteria
				 * @param offset   offset of result to return
				 * @param pageSize the size of returned set
				 * @return collection of stored metrics
				 */
				@Override
				public Collection<StoredMetric> find(SelectCriteria criteria, int offset, int pageSize) {
					return Collections.EMPTY_SET;
				}
			};
			return storage;
		}

		@Bean
		public ModuleConfigurationStorage makeModuleConfigurationStorage() {
			return mock(ModuleConfigurationStorage.class);
		}
	}

}