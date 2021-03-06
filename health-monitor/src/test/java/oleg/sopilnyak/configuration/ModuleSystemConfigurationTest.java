package oleg.sopilnyak.configuration;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.module.metric.storage.SelectCriteria;
import oleg.sopilnyak.module.metric.storage.StoredMetric;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.configuration.ModuleConfigurationService;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.control.ModuleCommand;
import oleg.sopilnyak.service.control.ModuleCommandFactory;
import oleg.sopilnyak.service.control.impl.ListModuleCommand;
import oleg.sopilnyak.service.control.impl.RestartModuleCommand;
import oleg.sopilnyak.service.control.impl.StartModuleCommand;
import oleg.sopilnyak.service.control.impl.StopModuleCommand;
import oleg.sopilnyak.service.metric.ActionMetricsContainer;
import oleg.sopilnyak.service.metric.DurationMetricsContainer;
import oleg.sopilnyak.service.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModuleSystemConfiguration.class, ModuleSystemConfigurationTest.Config.class})
public class ModuleSystemConfigurationTest {


	@Autowired
	private ApplicationContext context;


	@Test
	public void getMetricsContainer() {
		assertNotNull(context.getBean(MetricsContainer.class));
		assertNotNull(context.getBean(ActionMetricsContainer.class));
		assertNotNull(context.getBean(HeartBeatMetricContainer.class));
		assertNotNull(context.getBean(DurationMetricsContainer.class));
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

	@Test
	public void getModuleCommandFactory(){
		assertNotNull(context.getBean(ModuleCommandFactory.class));
	}

	@Test
	public void commandPrototypesTest() {
		ModuleCommand command = context.getBean(ListModuleCommand.class);
		assertNotEquals(command, context.getBean(ListModuleCommand.class));

		command = context.getBean(StartModuleCommand.class);
		assertNotEquals(command, context.getBean(StartModuleCommand.class));

		command = context.getBean(StartModuleCommand.class);
		assertNotEquals(command, context.getBean(StopModuleCommand.class));

		command = context.getBean(StopModuleCommand.class);
		assertNotEquals(command, context.getBean(StartModuleCommand.class));

		command = context.getBean(RestartModuleCommand.class);
		assertNotEquals(command, context.getBean(RestartModuleCommand.class));
	}

	@Test
	public void testDuring5seconds() throws InterruptedException {
		TimeUnit.SECONDS.sleep(5);
	}
	// inner class- configuration
	@Configuration
	public static class Config {
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
				public void storeMetric(String name, String module, Instant measured, String host, String metricAsString) {
					System.out.print("- metric ->");
					System.out.print(" name: " + name);
					System.out.print(" module: " + module);
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
		public ModuleConfigurationStorage getModuleConfigurationStorage() {
			ModuleConfigurationStorage storage = mock(ModuleConfigurationStorage.class);
			when(storage.getUpdatedVariables(any(Module.class), anyMap())).thenAnswer(new Answer<Map<String, VariableItem>>() {
				@Override
				public Map<String, VariableItem> answer(InvocationOnMock invocation) throws Throwable {
					return (Map<String, VariableItem>) invocation.getArguments()[1];
				}
			});
			return storage;
		}
	}

}