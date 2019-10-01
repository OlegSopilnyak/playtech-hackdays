/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleValues;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.ActionContext;
import oleg.sopilnyak.service.action.bean.ActionMapper;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MetricsContainerImplTest {
	@Mock
	private Module module;
	@Spy
	private TimeService timeService = new ModuleUtilityConfiguration().getTimeService();

	@InjectMocks
	private MetricsContainerImpl container = new MetricsContainerImpl();

	@Before
	public void setUp() {
		when(module.getMetricsContainer()).thenReturn(container);
	}

	@After
	public void tearDown() {
		reset(module);
		container.clear();
	}

	@Test
	public void testAdd() {
		Queue<ModuleMetric> metrics = (Queue<ModuleMetric>) ReflectionTestUtils.getField(container, "metrics");
		ModuleMetric metric = mock(ModuleMetric.class);
		container.add(metric);
		assertEquals(1, metrics.size());
	}

	@Test
	public void testAddCollection() {
		Queue<ModuleMetric> metrics = (Queue<ModuleMetric>) ReflectionTestUtils.getField(container, "metrics");
		ModuleMetric metric = mock(ModuleMetric.class);
		container.add(Collections.singletonList(metric));
		assertEquals(1, metrics.size());
	}

	@Test
	public void testUnProcessed() {
		assertEquals(0, container.unProcessed());
		ModuleMetric metric = mock(ModuleMetric.class);
		container.add(metric);
		assertEquals(1, container.unProcessed());
	}

	@Test
	public void testClear() {
		assertEquals(0, container.unProcessed());
		ModuleMetric metric = mock(ModuleMetric.class);
		container.add(metric);
		assertEquals(1, container.unProcessed());
		container.clear();
		assertEquals(0, container.unProcessed());
	}

	@Test
	public void testMetrics() {
		Collection<ModuleMetric> all = container.metrics();
		assertTrue(all.isEmpty());
		ModuleMetric metric = mock(ModuleMetric.class);
		container.add(metric);
		assertEquals(1, container.unProcessed());
		all = container.metrics();
		assertFalse(all.isEmpty());
		assertEquals(1, all.size());
	}

	@Test
	public void testAction() {
		assertEquals(container, container.action());
	}

	@Test
	public void testChanged() throws InterruptedException {
		ModuleAction parent = mock(ModuleAction.class);
		ModuleActionAdapter action =  ActionMapper.INSTANCE.simple(module, parent, "test");
		container.action().changed(action);

		assertEquals(-1L, action.getDuration().longValue());
		assertEquals(1, container.unProcessed());

		action.setState(ModuleAction.State.PROGRESS);
		container.action().changed(action);
		assertNotNull(action.getStarted());
		assertEquals(2, container.unProcessed());

		TimeUnit.MILLISECONDS.sleep(100);
		container.action().changed(action);
		assertEquals(3, container.unProcessed());
		assertTrue(action.getDuration() >= 100);
	}

	@Test
	public void testFail() {
		RuntimeException exception = new RuntimeException();
		ModuleAction parent = mock(ModuleAction.class);
		ModuleActionAdapter action =  ActionMapper.INSTANCE.simple(module, parent, "test");

		container.action().fail(action, exception);

		assertEquals(2, container.unProcessed());
		assertNull(action.getStarted());
		assertEquals(-1L, action.getDuration().longValue());
		assertEquals(ModuleAction.State.INIT, action.getState());
	}

	@Test
	public void testStart(){
		ModuleAction parent = mock(ModuleAction.class);
		ModuleActionAdapter action =  ActionMapper.INSTANCE.simple(module, parent, "test");
		ActionContext context = mock(ActionContext.class);

		container.action().start(action, context);

		assertEquals(1, container.unProcessed());
		verify(context, times(1)).getCriteria();
		verify(context, times(1)).getInput();
	}

	@Test
	public void testFinish(){
		ModuleAction parent = mock(ModuleAction.class);
		ModuleActionAdapter action =  ActionMapper.INSTANCE.simple(module, parent, "test");

		container.action().finish(action, "");

		assertEquals(1, container.unProcessed());
	}

	@Test
	public void testSuccess() {
		ModuleAction parent = mock(ModuleAction.class);
		ModuleActionAdapter action =  ActionMapper.INSTANCE.simple(module, parent, "test");

		container.action().success(action);

		assertEquals(2, container.unProcessed());
		assertNull(action.getStarted());
		assertEquals(-1L, action.getDuration().longValue());
		assertEquals(ModuleAction.State.INIT, action.getState());
	}

	@Test
	public void testHealth() {
		assertEquals(container, container.health());
	}

	@Test
	public void testHeartBeat() {
		ModuleAction parent = mock(ModuleAction.class);
		ModuleActionAdapter action =  ActionMapper.INSTANCE.simple(module, parent, "test");

		ModuleValues values = mock(ModuleValues.class);
		when(module.values()).thenReturn(Stream.of(values));

		container.health().heartBeat(action, module);

		assertEquals(1, container.unProcessed());
	}

	@Test
	public void testDuration() {
		assertEquals(container, container.duration());
	}

	@Test
	public void testSimple() {
		ModuleAction parent = mock(ModuleAction.class);
		ModuleActionAdapter action =  ActionMapper.INSTANCE.simple(module, parent, "test");

		container.duration().simple("test-simple", action, timeService.now(), module.primaryKey(), 10);

		assertEquals(1, container.unProcessed());
	}

	@Test
	public void testTotal() {
		ModuleAction parent = mock(ModuleAction.class);
		ModuleActionAdapter action =  ActionMapper.INSTANCE.simple(module, parent, "test");

		container.duration().total("test-total", action, timeService.now(), 25, 10);

		assertEquals(1, container.unProcessed());
	}
}