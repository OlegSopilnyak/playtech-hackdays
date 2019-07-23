package oleg.sopilnyak.service;

import ch.qos.logback.classic.Level;
import oleg.sopilnyak.configuration.ModuleSystemConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.module.model.action.ResultModuleAction;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.action.storage.ModuleActionStorageStub;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.service.model.dto.VariableItemDto;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static oleg.sopilnyak.module.model.ModuleHealthCondition.*;
import static oleg.sopilnyak.service.ModuleServiceAdapter.*;
import static oleg.sopilnyak.service.logging.ModuleLoggerService.LEVEL_NAME;
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
	public void testingModuleRestart(){
		assertTrue(service.isActive());
		reset(actionStorage, configurationStorage, service);

		service.restart();

		verify(service, times(1)).canRestart();
		verify(service, times(1)).moduleStop();
		verify(service, times(1)).shutdownAsService();
		verify(service, times(1)).moduleStart();
		verify(service, times(1)).initAsService();

		assertTrue(service.isActive());
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
	public void testingHealthGoUp() {
		assertEquals(VERY_GOOD, service.getCondition());

		service.healthCondition = FAIL;

		service.healthGoUp();
		assertEquals(POOR, service.getCondition());

		service.healthGoUp();
		assertEquals(AVERAGE, service.getCondition());

		service.healthGoUp();
		assertEquals(GOOD, service.getCondition());

		service.healthGoUp();
		assertEquals(VERY_GOOD, service.getCondition());

		service.healthGoUp();
		assertEquals(VERY_GOOD, service.getCondition());
	}

	@Test
	public void testingHealthGoLow() {
		assertTrue(service.isActive());

		Exception thrown = new Exception();
		assertEquals(VERY_GOOD, service.getCondition());

		service.healthGoLow(thrown);
		assertEquals(GOOD, service.getCondition());

		service.healthGoLow(thrown);
		assertEquals(AVERAGE, service.getCondition());

		service.healthGoLow(thrown);
		assertEquals(POOR, service.getCondition());

		service.healthGoLow(thrown);
		assertEquals(FAIL, service.getCondition());

		service.healthGoLow(thrown);
		assertEquals(FAIL, service.getCondition());
		assertFalse(service.isActive());

		verify(service, times(1)).moduleStop();
		verify(service, times(1)).shutdownAsService();
	}

	@Test
	public void testLastThrown() {
		Exception thrown = new Exception();
		assertEquals(VERY_GOOD, service.getCondition());

		service.healthGoLow(thrown);
		assertEquals(GOOD, service.getCondition());

		assertEquals(thrown, service.lastThrown());
	}

	@Test
	public void testingCanRestart() {
		assertTrue(service.canRestart());
	}

	@Test
	public void testingGetConfiguration() {
		Map<String, VariableItem> config = service.getConfiguration();
		assertNotNull(config);
		assertTrue(config.isEmpty());
		assertEquals(service.moduleConfiguration, config);
	}

	@Test
	public void testingConfigurationChanged() {
		Map<String, VariableItem> configuration = new HashMap<>();
		String variableName = "test-log-level";
		VariableItem variableValue = new VariableItemDto(LEVEL_NAME, Level.ERROR_INT);
		configuration.put(variableName, variableValue);
		reset(actionStorage, configurationStorage, service);

		service.configurationChanged(configuration);

		verify(service, times(3)).activateMainModuleAction();
		verify(service, times(1)).configurationItemChanged(eq(variableName), eq(variableValue));
		verify(service, times(1)).restart();
		verify(service, times(1)).canRestart();
		verify(service, times(1)).moduleStop();
		verify(service, times(1)).shutdownAsService();
		verify(service, times(1)).moduleStart();
		verify(service, times(1)).initAsService();
	}

	@Test
	public void testingGetMetricsContainer() {

		MetricsContainer container = service.getMetricsContainer();
		assertNotNull(container);
		assertEquals(service.metricsContainer, container);
	}

	@Test
	public void testingConfigurationVariableOf() {
		assertNull(service.configurationVariableOf(null));
		assertNull(service.configurationVariableOf(""));

		reset(actionStorage, configurationStorage, service);

		String variableName = "test-log-level";
		VariableItem variableValue = new VariableItemDto(LEVEL_NAME, Level.ERROR_INT);

		Map<String, VariableItem> configuration = mock(Map.class);
		when(configuration.get(variableName)).thenReturn(variableValue);
		when(service.getConfiguration()).thenReturn(configuration);

		VariableItem value = service.configurationVariableOf(variableName);

		verify(service, times(1)).isActive();
		verify(service, times(1)).getConfiguration();
		verify(configuration, times(1)).get(eq(variableName));

		assertEquals(variableValue, value);

		service.active = false;

		assertNull(service.configurationVariableOf(variableName));

		service.active = true;
	}

	@Test
	public void testActivateMainModuleAction() {
		reset(actionStorage, configurationStorage, service);

		service.activateMainModuleAction();

		verify(service, times(1)).getMainAction();
		verify(service, times(1)).getMetricsContainer();
	}

	@Test
	public void testFinishModuleAction() {
		reset(actionStorage, configurationStorage, service);

		service.finishModuleAction(true);

		verify(service, times(1)).getMainAction();
		verify(service, times(1)).getMetricsContainer();
	}

	@Test
	public void testInitAsService() {
		reset(actionStorage, configurationStorage, service);

		service.initAsService();

		verify(service, times(1)).serviceInitiated();
	}

	@Test
	public void testShutdownAsService() {
		reset(actionStorage, configurationStorage, service);

		service.shutdownAsService();

		verify(service, times(1)).serviceShutdown();
	}

	@Test
	public void testConfigurationItemChanged() {
		reset(actionStorage, configurationStorage, service);
		String variableName = "test-log-level";
		VariableItem variableValue = new VariableItemDto(LEVEL_NAME, Level.ERROR_INT);

		boolean success = service.configurationItemChanged(variableName, variableValue);

		assertTrue(success);
		verify(service, times(1)).updateConfiguration(eq(variableName), eq(variableValue));

	}

	@Test
	public void testExecuteAtomicAction() {
		Runnable testFunction = mock(Runnable.class);
		reset(actionStorage, configurationStorage, service);
		String activityName = "test-execution";

		// normal flow
		ResultModuleAction result = service.executeAtomicAction(activityName, testFunction);

		assertNotNull(result);
		assertNull(service.lastThrown());


		verify(service, times(3)).primaryKey();
		verify(service, times(5)).getMetricsContainer();
		verify(service, times(1)).healthGoUp();
		verify(actionStorage, times(1)).createActionFor(eq(service), any(ModuleAction.class), eq(activityName));
		verify(testFunction, times(1)).run();

		// exception flow
		RuntimeException exception = new RuntimeException("testing exception");
		doThrow(exception).when(testFunction).run();

		result = service.executeAtomicAction(activityName, testFunction);

		assertNotNull(result);
		assertNotNull(service.lastThrown());
		assertEquals(exception, service.lastThrown());
		verify(service, times(1)).healthGoLow(eq(exception));
	}

	@Test
	public void testSetupModuleConfiguration() {
		Map<String, VariableItem> config = service.getConfiguration();
		reset(actionStorage, configurationStorage, service);

		service.setupModuleConfiguration();

		verify(service, times(1)).getConfiguration();
		verify(service, times(1)).getMetricsContainer();
		verify(configurationStorage, times(1)).getUpdatedVariables(eq(service), eq(config));
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
		@Override
		protected boolean configurationItemChanged(String itemName, VariableItem itemValue) {
			updateConfiguration(itemName, itemValue);
			return true;
		}
		void updateConfiguration(String itemName, VariableItem itemValue){}

		@Override
		protected void initAsService() {
			serviceInitiated();
		}
		void serviceInitiated(){}

		@Override
		protected void shutdownAsService() {
			serviceShutdown();
		}
		void serviceShutdown(){}
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