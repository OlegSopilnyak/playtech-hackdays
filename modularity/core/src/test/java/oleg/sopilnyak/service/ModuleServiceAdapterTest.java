package oleg.sopilnyak.service;

import oleg.sopilnyak.configuration.ModuleSystemConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.action.storage.ModuleActionStorageStub;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static oleg.sopilnyak.module.model.ModuleHealthCondition.FAIL;
import static oleg.sopilnyak.module.model.ModuleHealthCondition.VERY_GOOD;
import static oleg.sopilnyak.service.ModuleServiceAdapter.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModuleServiceAdapterTest.Config.class})
public class ModuleServiceAdapterTest {

	@Autowired
	private ModuleActionStorage actionStorage;
	@Autowired
	private ModuleConfigurationStorage configurationStorage;
	@Autowired
	private ModulesRegistryService registry;

	@Autowired
	private TestService service;

	@Before
	public void setUp() {
		service.moduleStart();
	}

	@After
	public void tearDown() {
		service.healthCondition = VERY_GOOD;
		service.moduleStop();
		reset(actionStorage, configurationStorage, service);
	}

	@Test
	public void testingModuleStart() {
		service.moduleStop();
		assertFalse(service.isActive());

		assertEquals(ModuleAction.State.INIT, service.getMainAction().getState());
		reset(actionStorage, configurationStorage, service);

		service.moduleStart();

		verify(service,times(2)).getMainAction();
		verify(service, times(1)).activateMainModuleAction();
		verify(service, times(1)).executeAtomicAction(eq(CONFIGURE_MODULE_ACTION_NAME), any(Runnable.class));
		verify(service, times(1)).setupModuleConfiguration();
		verify(service, times(1)).getConfiguration();
		verify(service, times(1)).executeAtomicAction(eq(INIT_MODULE_ACTION_NAME), any(Runnable.class));
		verify(service, times(1)).initAsService();
		verify(service, times(2)).healthGoUp();
		verify(service, times(9)).primaryKey();
		verify(service, times(11)).getMetricsContainer();

		assertTrue(service.isActive());
		assertEquals(service, registry.getRegistered(service));
		assertEquals(ModuleAction.State.PROGRESS, service.getMainAction().getState());
		assertEquals(VERY_GOOD, service.getCondition());

		verify(actionStorage, atLeast(2)).createActionFor(any(Module.class), any(), anyString());
		verify(configurationStorage, times(1)).getUpdatedVariables(eq(service), anyMap());

	}

	@Test
	public void testingModuleStop() {
		assertTrue(service.isActive());

		assertEquals(ModuleAction.State.PROGRESS, service.getMainAction().getState());
		assertEquals(VERY_GOOD, service.getCondition());
		reset(actionStorage, configurationStorage, service);

		service.moduleStop();

		verify(service,times(2)).getMainAction();
		verify(service, times(1)).activateMainModuleAction();
		verify(service, times(1)).executeAtomicAction(eq(SHUTDOWN_MODULE_ACTION_NAME), any(Runnable.class));
		verify(service, times(1)).shutdownAsService();
		verify(service, times(1)).finishModuleAction(eq(true));
		verify(service, times(6)).primaryKey();
		verify(service, times(6)).getMetricsContainer();

		assertFalse(service.isActive());
		assertNull(registry.getRegistered(service));
		assertEquals(ModuleAction.State.INIT, service.getMainAction().getState());
		assertEquals(VERY_GOOD, service.getCondition());

		verify(actionStorage, atLeast(1)).createActionFor(any(Module.class), any(), anyString());
	}

	@Test
	public void testingGetMainAction() {
		reset(actionStorage, configurationStorage, service);

		ReflectionTestUtils.setField(service, "moduleMainAction", null);

		ModuleAction mainAction = service.getMainAction();

		assertNotNull(mainAction);
		assertEquals(mainAction, service.getMainAction());

		verify(actionStorage, times(1)).createActionFor(eq(service));
	}

	@Test
	public void testingIsActive() {
		assertTrue(service.isActive());

		service.active = false;

		assertFalse(service.isActive());

		service.active = true;
		assertTrue(service.isActive());
	}

	@Test
	public void testingGetCondition() {
		assertEquals(VERY_GOOD, service.getCondition());

		service.healthCondition = FAIL;
		assertEquals(FAIL, service.getCondition());

		service.healthCondition = VERY_GOOD;
		assertEquals(VERY_GOOD, service.getCondition());
	}

	@Test
	public void healthGoUp() {
	}

	@Test
	public void healthGoLow() {
	}

	@Test
	public void lastThrown() {
	}

	@Test
	public void canRestart() {
	}

	@Test
	public void getConfiguration() {
	}

	@Test
	public void configurationChanged() {
	}

	@Test
	public void getMetricsContainer() {
	}

	@Test
	public void configurationVariableOf() {
	}

	@Test
	public void activateMainModuleAction() {
	}

	@Test
	public void finishModuleAction() {
	}

	@Test
	public void initAsService() {
	}

	@Test
	public void shutdownAsService() {
	}

	@Test
	public void configurationItemChanged() {
	}

	@Test
	public void executeAtomicAction() {
	}

	@Test
	public void setupModuleConfiguration() {
	}

	//inner classes
	static class TestService extends ModuleServiceAdapter{
		@Override
		public String getSystemId() {
			return "modules";
		}
		@Override
		public String getModuleId() {
			return "just-test";
		}
		@Override
		public String getVersionId() {
			return "0.8";
		}
		@Override
		public String getDescription() {
			return "Test service for deep testing.";
		}
	}
	@Configuration
	@Import({ModuleSystemConfiguration.class})
	static class Config{

		@Bean
		public TestService makeTestService(){
			return spy(new TestService());
		}
		@Bean
		public ModuleActionStorage mockModuleActionStorage(){
			return spy(new ModuleActionStorageStub());
		}
		@Bean
		public ModuleConfigurationStorage mockModuleConfigurationStorage(){
			return mock(ModuleConfigurationStorage.class);
		}
		@Bean
		public ModuleMetricStorage mockModuleMetricStorage(){
			return mock(ModuleMetricStorage.class);
		}
	}
}