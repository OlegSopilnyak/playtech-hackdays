package oleg.sopilnyak.configuration;

import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.configuration.ModuleConfigurationService;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.metric.ActionMetricsContainer;
import oleg.sopilnyak.service.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModuleSystemConfiguration.class, ModuleSystemConfigurationTest.class})
public class ModuleSystemConfigurationTest {


	@Autowired
	private ApplicationContext context;

	@Bean
	public ModuleMetricStorage getModuleMetricStorage() {
		return mock(ModuleMetricStorage.class);
	}

	@Bean
	public ModuleConfigurationStorage getModuleConfigurationStorage() {
		return mock(ModuleConfigurationStorage.class);
	}

	@Test
	public void getMetricsContainer() {
		assertNotNull(context.getBean(MetricsContainer.class));
		assertNotNull(context.getBean(ActionMetricsContainer.class));
		assertNotNull(context.getBean(HeartBeatMetricContainer.class));
	}

	@Test
	public void getModuleActionFactory() {
		assertNotNull(context.getBean(ModuleActionFactory.class));
	}

	@Test
	public void getModuleConfigurationService() {
		assertNotNull(context.getBean(ModuleConfigurationService.class));
	}

	@Test
	public void getHealthModuleService() {
		assertNotNull(context.getBean(ModulesRegistry.class));
	}
}