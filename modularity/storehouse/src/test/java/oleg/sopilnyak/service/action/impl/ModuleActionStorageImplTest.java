/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.impl;

import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.DurationMetricsContainer;
import oleg.sopilnyak.module.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.action.ModuleActionsRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.StringUtils;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModuleActionStorageImplTest {
	@Mock
	private MetricsContainer metricsContainer;
	@Mock
	private ActionMetricsContainer actionMetricsContainer;
	@Mock
	private DurationMetricsContainer durationMetricsContainer;
	@Mock
	private HeartBeatMetricContainer heartBeatMetricContainer;
	@Spy
	private UniqueIdGenerator idGenerator = new ModuleUtilityConfiguration().getUniqueIdGenerator();
	@Mock
	private ModuleActionsRepository repository;

	@InjectMocks
	private ModuleActionStorageImpl storage = new ModuleActionStorageImpl();

	@Before
	public void setUp() {
		when(metricsContainer.action()).thenReturn(actionMetricsContainer);
		when(metricsContainer.duration()).thenReturn(durationMetricsContainer);
		when(metricsContainer.health()).thenReturn(heartBeatMetricContainer);
	}

	@After
	public void tearDown() throws Exception {
		reset( repository, idGenerator);
	}

	@Test
	public void testSetUp() {
		assertTrue(StringUtils.isEmpty(storage.hostName));
		storage.setUp();
		assertFalse(StringUtils.isEmpty(storage.hostName));
		assertNotEquals("localhost", storage.hostName);
	}

	@Test
	public void createActionForMain() {
		Module module = mock(Module.class);
		when(module.getMetricsContainer()).thenReturn(metricsContainer);

		ModuleAction action = storage.createActionFor(module);

		assertTrue(action.getName().startsWith("[main"));
		assertFalse(StringUtils.isEmpty(action.getId()));
		assertEquals(storage.hostName, action.getHostName());
		assertTrue(action.getDescription().startsWith("Main"));

		verify(idGenerator,times(1)).generate();
	}

	@Test
	public void createActionForRegular() {
		Module module = mock(Module.class);
		ModuleAction parent = mock(ModuleAction.class);
		String name = "test";

		when(module.getMetricsContainer()).thenReturn(metricsContainer);

		ModuleAction action = storage.createActionFor(module, parent, name);

		assertEquals("["+name+"-action]", action.getName());
		assertEquals(parent, action.getParent());
		assertFalse(StringUtils.isEmpty(action.getId()));
		assertEquals(storage.hostName, action.getHostName());
		assertTrue(action.getDescription().startsWith(name));

		verify(idGenerator,times(1)).generate();
		verify(actionMetricsContainer, times(1)).changed(any(ModuleAction.class));
	}

	@Test
	public void createActionForRegular2() {
		Module module = mock(Module.class);
		ModuleAction parent = mock(ModuleAction.class);
		String name = "test";

		when(module.getMetricsContainer()).thenReturn(metricsContainer);
		when(module.getMainAction()).thenReturn(parent);

		ModuleAction action = storage.createActionFor(module, null, name);

		assertEquals("["+name+"-action]", action.getName());
		assertEquals(parent, action.getParent());
		assertFalse(StringUtils.isEmpty(action.getId()));
		assertEquals(storage.hostName, action.getHostName());
		assertTrue(action.getDescription().startsWith(name));

		verify(idGenerator,times(1)).generate();
		verify(actionMetricsContainer, times(1)).changed(any(ModuleAction.class));
	}

	@Test
	public void persist() {
		ModuleAction action = mock(ModuleAction.class);

		storage.persist(action);

		verify(repository,times(1)).persist(eq(action));
	}

	@Test
	public void getById() {
		ModuleAction action = mock(ModuleAction.class);

		when(repository.getById("test")).thenReturn(action);

		assertEquals(action, storage.getById("test"));
		assertNull(storage.getById("test-1"));

		verify(repository,times(1)).getById(eq("test"));
		verify(repository,times(1)).getById(eq("test-1"));
	}
}