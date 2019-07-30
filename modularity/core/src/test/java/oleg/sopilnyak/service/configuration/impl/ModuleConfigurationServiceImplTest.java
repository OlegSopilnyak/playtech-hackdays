/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.impl;

import oleg.sopilnyak.configuration.ModuleSystemConfiguration;
import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.DurationMetricsContainer;
import oleg.sopilnyak.module.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.action.bean.ActionMapper;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;
import oleg.sopilnyak.service.action.impl.ModuleActionFactoryImpl;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.model.dto.VariableItemDto;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
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
	private ModuleActionFactory actionsFactory = new ModuleSystemConfiguration().getModuleActionFactory();
	@Mock
	private ModuleConfigurationStorage configurationStorage;
	@Mock
	private ModuleActionStorage actionStorage;
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

		mainAction = ActionMapper.INSTANCE.simple(module, "test");
		when(module.getMainAction()).thenReturn(mainAction);

		when(metricsContainer.action()).thenReturn(actionMetricsContainer);
		when(metricsContainer.duration()).thenReturn(durationMetricsContainer);
		when(metricsContainer.health()).thenReturn(heartBeatMetricContainer);

		prepareActionsFactory();


		ReflectionTestUtils.setField(service, "moduleMainAction", null);

		((ModuleActionFactoryImpl) actionsFactory).setUp();

		service.moduleStart();
	}

	@After
	public void tearDown() {
		service.moduleStop();
		reset(module, actionsFactory, metricsContainer, actionMetricsContainer, registry, activityRunner);
	}


	@Test
	public void testInitAsService() {
		ModuleConfigurationStorage.ConfigurationListener listener =
				(ModuleConfigurationStorage.ConfigurationListener) ReflectionTestUtils.getField(service, "storageListener");

		service.moduleStop();
		// after setup we have activityRunner runs once
		reset(activityRunner);

		service.initAsService();

		verify(configurationStorage, times(1)).addConfigurationListener(eq(listener));
		verify(activityRunner, times(1)).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

		ScheduledFuture runnerFuture = (ScheduledFuture) ReflectionTestUtils.getField(service, "runnerFuture");
		assertNotNull(runnerFuture);
		assertFalse(runnerFuture.isDone());
	}

	@Test
	public void testShutdownAsService() {
		ModuleConfigurationStorage.ConfigurationListener listener =
				(ModuleConfigurationStorage.ConfigurationListener) ReflectionTestUtils.getField(service, "storageListener");

		service.shutdownAsService();

		ScheduledFuture runnerFuture = (ScheduledFuture) ReflectionTestUtils.getField(service, "runnerFuture");
		ScheduledFuture notifyFuture = (ScheduledFuture) ReflectionTestUtils.getField(service, "notifyFuture");
		assertNull(runnerFuture);
		assertNull(notifyFuture);
		verify(configurationStorage, times(1)).removeConfigurationListener(eq(listener));
	}

	@Test
	public void testInspectModule() {
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
	public void testScanModulesConfiguration() {
		reset(timeService);

		Map<String, VariableItem> config = new HashMap<>();
		config.put("1.2.3.4.value", new VariableItemDto("value", 100));
		when(module.getConfiguration()).thenReturn(config);
		when(configurationStorage.getUpdatedVariables(module, config)).thenReturn(config).thenReturn(Collections.emptyMap());

		when(registry.registered()).thenReturn(Collections.singletonList(module));

		service.scanModulesConfiguration();

		verify(actionsFactory, times(1)).executeAtomicModuleAction(eq(service), eq("configuration-check"), any(Runnable.class), eq(false));
		verify(actionsFactory, times(2)).startMainAction(eq(service));
		verify(actionsFactory, times(2)).currentAction();

		verify(module, times(1)).getConfiguration();
		verify(configurationStorage, times(1)).getUpdatedVariables(eq(module), eq(config));
		// changing module configuration
		verify(module, times(1)).configurationChanged(eq(config));
	}

	@Test
	public void testRunNotificationProcessing() throws InterruptedException {
		reset(actionsFactory, activityRunner);
		String testModule = "testModule";

		service.runNotificationProcessing(Collections.singleton(testModule));

		TimeUnit.MILLISECONDS.sleep(100);

		verify(activityRunner, times(2)).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
		verify(actionsFactory, atLeastOnce()).startMainAction(eq(service));
	}

	@Test
	public void testScheduleScan() {
		reset(actionsFactory);

		String testModule = "testModule";

		BlockingQueue<Collection<String>> storageChangesQueue = (BlockingQueue<Collection<String>>) ReflectionTestUtils.getField(service, "storageChangesQueue");
		storageChangesQueue.add(Collections.singleton(testModule));

		service.scheduleScan();

		verify(activityRunner, times(1)).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
		verify(actionsFactory, times(1)).startMainAction(eq(service));
	}

	@Test
	public void testWaitForFutureDone() {
		service.waitForFutureDone(null);

		ScheduledFuture future = activityRunner.schedule(() -> System.out.println("test wait for"), 50, TimeUnit.MILLISECONDS);

		service.waitForFutureDone(future);

		assertFalse(future.isCancelled());
		assertTrue(future.isDone());
	}

	@Test
	public void testStopFuture() {
		service.stopFuture(null);

		ScheduledFuture future = activityRunner.schedule(() -> System.out.println("test stop"), 50, TimeUnit.MILLISECONDS);

		service.stopFuture(future);

		assertTrue(future.isCancelled());
		assertTrue(future.isDone());
	}

	// private methods
	private void prepareActionsFactory() {
		ReflectionTestUtils.setField(actionsFactory, "scanRunner", activityRunner);
		ReflectionTestUtils.setField(actionsFactory, "delay", 200L);
		ReflectionTestUtils.setField(actionsFactory, "actionsStorage", actionStorage);
		when(actionStorage.createActionFor(any(Module.class)))
				.thenAnswer((Answer<ModuleAction>) invocation -> ActionMapper.INSTANCE.simple((ModuleBasics) invocation.getArguments()[0], "main-test"));
		when(actionStorage.createActionFor(any(Module.class), any(ModuleAction.class), anyString())).thenAnswer((Answer<ModuleAction>) invocation -> {
			ModuleActionAdapter result1 = ActionMapper.INSTANCE.simple((ModuleBasics) invocation.getArguments()[0], "regular-test");
			result1.setParent((ModuleAction) invocation.getArguments()[1]);
			result1.setName((String) invocation.getArguments()[2]);
			return result1;
		});
	}

}