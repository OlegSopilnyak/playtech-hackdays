/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.impl;

import oleg.sopilnyak.configuration.ModuleSystemConfiguration;
import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.DurationMetricsContainer;
import oleg.sopilnyak.module.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.module.model.action.ResultModuleAction;
import oleg.sopilnyak.module.model.action.SuccessModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.action.impl.ModuleActionFactoryImpl;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.dto.VariableItemDto;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModuleConfigurationServiceImplTest {
	@Mock
	private Module module;
	@Spy
	private ModuleActionAdapter mainAction;
	@Mock
	private MetricsContainer metricsContainer;
	@Mock
	private ActionMetricsContainer actionMetricsContainer;
	@Mock
	private DurationMetricsContainer durationMetricsContainer;
	@Mock
	private HeartBeatMetricContainer heartBeatMetricContainer;
	@Spy
	protected ScheduledExecutorService activityRunner = new ModuleUtilityConfiguration().getScheduledExecutorService();
	@Spy
	private TimeService timeService = new ModuleUtilityConfiguration().getTimeService();
	@Spy
	private UniqueIdGenerator idGenerator = new ModuleUtilityConfiguration().getUniqueIdGenerator();
	@Spy
	private ModuleActionFactory actionsFactory = new ModuleSystemConfiguration().getModuleActionFactory();
	@Mock
	private ModuleConfigurationStorage configurationStorage;
	@Mock
	private ModulesRegistryService registry;
	@InjectMocks
	private ModuleConfigurationServiceImpl service = new ModuleConfigurationServiceImpl();

	@Before
	public void setUp() {
		when(module.getSystemId()).thenReturn("sys-test");
		when(module.getModuleId()).thenReturn("mod-test");
		when(module.getVersionId()).thenReturn("ver-test");
		when(module.getDescription()).thenReturn("desc-test");
		when(module.primaryKey()).thenReturn("test-pk");
		mainAction = new ModuleActionAdapter(module, "test");
		ResultModuleAction result = new SuccessModuleAction(mainAction);
		when(metricsContainer.action()).thenReturn(actionMetricsContainer);
		when(metricsContainer.duration()).thenReturn(durationMetricsContainer);
		when(metricsContainer.health()).thenReturn(heartBeatMetricContainer);

		((ModuleActionFactoryImpl) actionsFactory).setUp();
		mainAction.setHostName((String) ReflectionTestUtils.getField(actionsFactory, "hostName"));
		ReflectionTestUtils.setField(actionsFactory, "idGenerator", idGenerator);

		service.moduleStart();
	}

	@After
	public void tearDown() {
		service.moduleStop();
		reset(module, actionsFactory, metricsContainer, actionMetricsContainer, registry);
	}


	@Test
	public void initAsService() {
		ModuleConfigurationStorage.ConfigurationListener listener = (ModuleConfigurationStorage.ConfigurationListener) ReflectionTestUtils.getField(service, "storageListener");

		service.initAsService();

		verify(configurationStorage, times(0)).addConfigurationListener(eq(listener));
		verify(activityRunner, times(0)).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

		service.moduleStop();

		service.initAsService();

		verify(configurationStorage, times(1)).addConfigurationListener(eq(listener));
		verify(activityRunner, times(1)).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

		ScheduledFuture runnerFuture = (ScheduledFuture) ReflectionTestUtils.getField(service, "runnerFuture");
		assertNotNull(runnerFuture);
		assertFalse(runnerFuture.isDone());
	}

	@Test
	public void shutdownAsService() {
		ModuleConfigurationStorage.ConfigurationListener listener = (ModuleConfigurationStorage.ConfigurationListener) ReflectionTestUtils.getField(service, "storageListener");

		service.shutdownAsService();

		ScheduledFuture runnerFuture = (ScheduledFuture) ReflectionTestUtils.getField(service, "runnerFuture");
		ScheduledFuture notifyFuture = (ScheduledFuture) ReflectionTestUtils.getField(service, "notifyFuture");
		assertNull(runnerFuture);
		assertNull(notifyFuture);
		verify(configurationStorage, times(1)).removeConfigurationListener(eq(listener));
	}

	@Test
	public void inspectModule() {
		reset(timeService);

		Map<String, VariableItem> config = new HashMap<>();
		config.put("1.2.3.4.value", new VariableItemDto("value", 100));
		when(module.getConfiguration()).thenReturn(config);
		when(configurationStorage.getUpdatedVariables(module, config)).thenReturn(config).thenReturn(Collections.emptyMap());

		service.inspectModule("test", mainAction, module);

		verify(timeService, times(3)).now();
		verify(timeService, times(1)).duration(any(Instant.class));
		verify(module, times(1)).getConfiguration();
		verify(configurationStorage, times(1)).getUpdatedVariables(eq(module), eq(config));
		// changing module configuration
		verify(module, times(1)).configurationChanged(eq(config));
		verify(durationMetricsContainer, times(1)).simple(eq("test"), eq(mainAction), any(Instant.class), anyString(), anyLong());

		service.inspectModule("test", mainAction, module);
		verify(durationMetricsContainer, times(2)).simple(eq("test"), eq(mainAction), any(Instant.class), anyString(), anyLong());
		verify(timeService, times(6)).now();
		verify(timeService, times(2)).duration(any(Instant.class));
		verify(module, times(2)).getConfiguration();
		verify(configurationStorage, times(2)).getUpdatedVariables(eq(module), eq(config));
		// no module configuration updates
		verify(module, times(1)).configurationChanged(eq(config));
	}

	@Test
	public void scanModulesConfiguration() {
	}

	@Test
	public void runNotificationProcessing() {
	}

	@Test
	public void scheduleScan() {
	}

	@Test
	public void waitForFutureDone() {
	}

	@Test
	public void stopFuture() {
	}
}