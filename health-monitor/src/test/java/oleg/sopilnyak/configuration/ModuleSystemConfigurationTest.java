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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModuleSystemConfiguration.class, ModuleSystemConfigurationTest.class})
public class ModuleSystemConfigurationTest {


	@Autowired
	private ApplicationContext context;

	@Bean
	public ModuleMetricStorage getModuleMetricStorage() {
		ModuleMetricStorage storage = mock(ModuleMetricStorage.class);
		doNothing().doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock inv) throws Throwable {
				Object[] args = inv.getArguments();
				System.out.print("-------");
				System.out.print(" name:"+args[0]);
				System.out.print(" module:"+args[1]);
				System.out.print(" measured:"+args[2]);
				System.out.print(" host:"+args[3]);
				System.out.println(" message:"+args[4]);
				return null;
			}
		}).when(storage).storeMetric(anyString(),anyString(), any(Instant.class),anyString(), anyString());
		return storage;
	}

	@Bean
	public ModuleConfigurationStorage getModuleConfigurationStorage() {
		ModuleConfigurationStorage storage = mock(ModuleConfigurationStorage.class);
		return storage;
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
	@Test
	public void testDuring5seconds() throws InterruptedException {
		TimeUnit.SECONDS.sleep(5);
	}
}