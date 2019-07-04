package oleg.sopilnyak.service.logging.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import oleg.sopilnyak.configuration.ModuleSystemConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.action.storage.ModuleActionStorageStub;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.logging.ModuleLoggerService;
import oleg.sopilnyak.service.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.service.model.dto.VariableItemDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static oleg.sopilnyak.service.logging.ModuleLoggerService.LEVEL_DEFAULT;
import static oleg.sopilnyak.service.logging.ModuleLoggerService.LEVEL_NAME;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		ModuleSystemConfiguration.class
		, ModuleSlf4jLogAppenderTest.Config.class
})
public class ModuleSlf4jLogAppenderTest {


	@Autowired
	private ModuleActionStorage actionStorage;

	private ModuleAction mainAction = mock(ModuleAction.class);

	@Autowired
	private ModuleSlf4jLogAppender service;


	@Before
	public void setUp() {
		service.setSeverityLevel(Level.toLevel(LEVEL_DEFAULT));
		service.setLayoutPattern(ModuleLoggerService.PATTERN_DEFAULT);
		service.moduleStart();
	}

	@After
	public void tearDown() {
		reset(actionStorage);
		service.moduleStop();
	}

	@Test
	public void testingModuleStart() {
		service.moduleStop();
		assertFalse(service.isStarted() || service.isActive());

		reset(actionStorage);
		service.moduleStart();

		assertTrue(service.isStarted() && service.isActive());
		verify(actionStorage, atLeast(2)).createActionFor(any(Module.class), any(), anyString());
	}

	@Test
	public void testingRegisterAppender() {
		service.moduleStop();
		assertFalse(service.isStarted() || service.isActive());

		service.registerAppender();
		assertTrue(service.isStarted() && !service.isActive());
	}

	@Test
	public void testingModuleStop() {
		service.moduleStop();
		assertFalse(service.isStarted() || service.isActive());
	}

	@Test
	public void testingUnRegisterAppender() {

		service.unRegisterAppender();
		assertTrue(!service.isStarted() && service.isActive());
	}

	@Test
	public void testingAppend() {

		ILoggingEvent event = mock(ILoggingEvent.class);
		when(event.getLevel()).thenReturn(Level.OFF);
		when(event.getLoggerName()).thenReturn("test-logger");
		service.getMetricsContainer().clear();

		service.append(event);

		assertEquals(1, service.getMetricsContainer().unProcessed());
	}

	@Test
	public void testingSerSeverityLevel() {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		assertNotEquals(Level.ERROR, logger.getLevel());

		service.setSeverityLevel(Level.ERROR);
		assertEquals(Level.ERROR, logger.getLevel());
	}

	@Test
	public void testingSetLayoutPattern() {
		service.setLayoutPattern("test");

		assertEquals("test", service.layoutPattern);
	}

	@Test
	public void testingIsActive() {
		assertTrue(service.isActive());

		service.moduleStop();

		assertFalse(service.isActive());
	}

	@Test
	public void testingGetCondition() {
		assertEquals(ModuleHealthCondition.VERY_GOOD, service.getCondition());
	}

	@Test
	public void testingHealthGoLow() {
		service.healthGoLow(new Exception());

		assertEquals(ModuleHealthCondition.GOOD, service.getCondition());
	}

	@Test
	public void testingLastThrown() {
		Exception ex = new Exception();

		service.healthGoLow(ex);
		assertEquals(ModuleHealthCondition.GOOD, service.getCondition());
		assertEquals(ex, service.lastThrown());
	}

	@Test
	public void testingHealthGoUp() {
		service.healthGoLow(new Exception());
		service.healthGoLow(new Exception());
		service.healthGoLow(new Exception());

		assertEquals(ModuleHealthCondition.POOR, service.getCondition());

		service.healthGoUp();
		assertEquals(ModuleHealthCondition.AVERAGE, service.getCondition());
	}

	@Test
	public void testingCanRestart() {
		assertTrue(service.canRestart());
	}

	@Test
	public void testingGetMainAction() {
		assertNotNull(service.getMainAction());
	}

	@Test
	public void testingGetMetricsContainer() {
		MetricsContainer container = service.getMetricsContainer();
		assertNotNull(container);
		assertTrue(container.unProcessed() > 0);
	}

	@Test
	public void testingGetConfiguration() {
		Map configuration = service.getConfiguration();
		assertFalse(configuration.isEmpty());
	}

	@Test
	public void testingConfigurationChanged() {
		assertEquals(LEVEL_DEFAULT, service.levelSeverity);

		Map<String, VariableItem> config = new HashMap<>();
		config.put(service.levelName(), new VariableItemDto(LEVEL_NAME, Level.ERROR_INT));

		service.configurationChanged(config);

		assertEquals(Level.ERROR_INT, service.levelSeverity);
	}

	@Test
	public void testingConfigurationItemChanged() {
		assertEquals(LEVEL_DEFAULT, service.levelSeverity);

		service.configurationItemChanged(service.levelName(), new VariableItemDto(LEVEL_NAME, Level.ERROR_INT));

		assertEquals(Level.ERROR_INT, service.levelSeverity);
	}
	// inner classes
	@Configuration
	static class Config{

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