/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.registry.impl;

import oleg.sopilnyak.configuration.ModuleSystemConfiguration;
import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.DurationMetricsContainer;
import oleg.sopilnyak.module.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.ServiceModule;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.action.bean.ActionMapper;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;
import oleg.sopilnyak.service.action.impl.ModuleActionFactoryImpl;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.registry.storage.ModuleStorage;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModuleRegistryServiceImplTest {

	@Mock
	private Module module;
	@Mock
	private ModuleStorage moduleStorage;
	@Mock
	private MetricsContainer metricsContainer;
	@Mock
	private ActionMetricsContainer actionMetricsContainer;
	@Mock
	private DurationMetricsContainer durationMetricsContainer;
	@Mock
	private HeartBeatMetricContainer heartBeatMetricContainer;
	@Mock
	private ModuleConfigurationStorage configurationStorage;
	@Spy
	private TimeService timeService = new ModuleUtilityConfiguration().getTimeService();
	@Spy
	private ModuleActionFactory actionsFactory = new ModuleSystemConfiguration().getModuleActionFactory();
	@Mock
	private ModuleActionStorage actionStorage;
	@Spy
	protected ScheduledExecutorService activityRunner = new ModuleUtilityConfiguration().getScheduledExecutorService();
	@InjectMocks
	private ModuleRegistryServiceImpl service = new ModuleRegistryServiceImpl();

	@Before
	public void setUp() {
		when(module.getSystemId()).thenReturn("sys-test");
		when(module.getModuleId()).thenReturn("mod-test");
		when(module.getVersionId()).thenReturn("ver-test");
		when(module.getDescription()).thenReturn("desc-test");
		when(module.primaryKey()).thenReturn("test-pk");
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
		reset(module, moduleStorage, actionStorage, actionsFactory, metricsContainer, actionMetricsContainer);
	}

	@Test
	public void testAdd() {
		service.register(module);

		Map<String, Module> modules = (Map<String, Module>) ReflectionTestUtils.getField(service, "modules");
		assertEquals(module, modules.get(module.primaryKey()));
		verify(actionsFactory, times(1)).startMainAction(eq(service));
	}

	@Test(expected = AssertionError.class)
	public void testAddFail() {
		service.register(null);
		fail("Here I'm waiting for an exception.");
	}

	@Test
	public void testRemove() {
		service.register(module);

		Map<String, Module> modules = (Map<String, Module>) ReflectionTestUtils.getField(service, "modules");
		assertEquals(module, modules.get(module.primaryKey()));

		service.remove(module);
		assertNull(modules.get(module.primaryKey()));

		verify(actionsFactory, times(1)).startMainAction(eq(service));
	}

	@Test(expected = AssertionError.class)
	public void testRemoveFail() {
		service.register(module);

		Map<String, Module> modules = (Map<String, Module>) ReflectionTestUtils.getField(service, "modules");
		assertEquals(module, modules.get(module.primaryKey()));

		service.remove(null);
		fail("Here I'm waiting for an exception.");
	}

	@Test
	public void testRegistered() {
		service.register(module);
		when(module.isModuleRegistered()).thenReturn(true);

		List<Module> modules = new ArrayList<>(service.registered());
		assertTrue(modules.contains(module));
	}

	@Test
	public void testGetRegistered() {
		assertNull(service.getRegistered(module));

		service.register(module);
		assertEquals(module, service.getRegistered(module));
	}

	@Test(expected = AssertionError.class)
	public void testGetRegisteredFail() {
		service.getRegistered((Module) null);
		fail("Here I'm waiting for an exception.");
	}

	@Test
	public void testGetRegisteredPK() {
		assertNull(service.getRegistered(module.primaryKey()));
		service.register(module);

		assertEquals(module, service.getRegistered(module.primaryKey()));
		assertNull(service.getRegistered((String) null));
		assertNull(service.getRegistered(""));
	}

	@Test
	public void testInitAsService() {
		service.moduleStop();

		service.initAsService();

		assertEquals(service, service.getRegistered(service));
	}

	@Test
	public void testShutdownAsService() {
		service.shutdownAsService();

		assertNull(service.getRegistered(service));
	}
	// private methods
	private void prepareActionsFactory() {
		ReflectionTestUtils.setField(actionsFactory, "timeService", timeService);
		ReflectionTestUtils.setField(actionsFactory, "scanRunner", activityRunner);
		ReflectionTestUtils.setField(actionsFactory, "delay", 200L);
		ReflectionTestUtils.setField(actionsFactory, "actionsStorage", actionStorage);
		when(actionStorage.createActionFor(any(Module.class)))
				.thenAnswer((Answer<ModuleAction>) invocation -> ActionMapper.INSTANCE.simple((ModuleBasics) invocation.getArguments()[0], "main-test"));
		when(actionStorage.createActionFor(any(ServiceModule.class), any(ModuleAction.class), anyString())).thenAnswer((Answer<ModuleAction>) invocation -> {
			ModuleActionAdapter result1 = ActionMapper.INSTANCE.simple((ModuleBasics) invocation.getArguments()[0], "regular-test");
			result1.setParent((ModuleAction) invocation.getArguments()[1]);
			result1.setName((String) invocation.getArguments()[2]);
			return result1;
		});
	}

}