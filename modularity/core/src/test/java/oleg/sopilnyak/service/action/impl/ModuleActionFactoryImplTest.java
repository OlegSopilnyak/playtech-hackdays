/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.impl;

import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.DurationMetricsContainer;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.ActionContext;
import oleg.sopilnyak.service.action.AtomicModuleAction;
import oleg.sopilnyak.service.action.bean.ActionMapper;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;
import oleg.sopilnyak.service.action.bean.result.ResultModuleAction;
import oleg.sopilnyak.service.action.exception.ModuleActionRuntimeException;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.model.DtoMapper;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.util.StringUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModuleActionFactoryImplTest {

	@Mock
	private Module module;
	@Mock
	private MetricsContainer metricsContainer;
	@Mock
	private ActionMetricsContainer actionMetricsContainer;
	@Mock
	private DurationMetricsContainer durationMetricsContainer;
	@Mock
	private ModuleActionStorage actionStorage;

	@Spy
	private ScheduledExecutorService runner = new ScheduledThreadPoolExecutor(2);
	@Spy
	private TimeService timeService = new ModuleUtilityConfiguration().getTimeService();

	@InjectMocks
	private ModuleActionFactoryImpl factory = spy(new ModuleActionFactoryImpl());

	@Before
	public void setUp() throws Exception {
		when(module.getSystemId()).thenReturn("sys-test");
		when(module.getModuleId()).thenReturn("mod-test");
		when(module.getDescription()).thenReturn("desc-test");
		when(module.primaryKey()).thenReturn("test-module-primary-key");

		when(module.getMetricsContainer()).thenReturn(metricsContainer);
		when(metricsContainer.action()).thenReturn(actionMetricsContainer);
		when(metricsContainer.duration()).thenReturn(durationMetricsContainer);

		prepareActionStorage();

		factory.delay = 200L;
	}

	@After
	public void tearDown() {
		reset(module, metricsContainer, actionMetricsContainer);
		factory.current.remove();
	}

	@Test
	public void testSetUp() {
		factory.setUp();
		verify(runner, times(1)).schedule(any(Runnable.class), eq(200L), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void testCreateModuleMainAction() {
		factory.setUp();
		ModuleAction action = factory.createModuleMainAction(module);
		ModuleDto moduleDto = DtoMapper.INSTANCE.toModuleDto(module);
		assertEquals(moduleDto, action.getModule());
		assertEquals(ModuleAction.State.INIT, action.getState());
		Assert.assertTrue(StringUtils.isEmpty(action.getId()));
		verify(actionMetricsContainer, times(1)).changed(eq(action));
	}

	@Test
	public void testCreateModuleRegularAction() {
		factory.setUp();
		ModuleAction action = factory.createModuleRegularAction(module, "test");
		ModuleDto moduleDto = DtoMapper.INSTANCE.toModuleDto(module);
		assertEquals(moduleDto, action.getModule());
		assertEquals(ModuleAction.State.INIT, action.getState());
		Assert.assertTrue(StringUtils.isEmpty(action.getId()));
		verify(actionMetricsContainer, times(1)).changed(eq(action));
	}

	@Test
	public void testExecuteAtomicModuleActionGood() {
		factory.setUp();
		AtomicInteger value = new AtomicInteger(0);
		reset(actionMetricsContainer);

		ModuleAction result = factory.executeAtomicModuleAction(module, "test", () -> value.getAndSet(100), true);

		ModuleDto moduleDto = DtoMapper.INSTANCE.toModuleDto(module);
		Assert.assertNotNull(result);
		Assert.assertNull(((ResultModuleAction) result).getCause());
		assertEquals(ModuleAction.State.PROGRESS, result.getState());
		assertEquals(moduleDto, result.getModule());
		assertEquals(100, value.get());
		verify(module, times(1)).healthGoUp();
		verify(actionMetricsContainer, times(2)).changed(any(ModuleAction.class));
		verify(actionMetricsContainer, times(1)).success(any(ModuleAction.class));
	}

	@Test
	public void testExecuteAtomicModuleActionBadNoRethrow() {
		factory.setUp();
		RuntimeException exception = new RuntimeException("test");
		Runnable throwException = () -> {
			throw exception;
		};
		reset(actionMetricsContainer);

		ModuleAction result = factory.executeAtomicModuleAction(module, "test", throwException, false);

		ModuleDto moduleDto = DtoMapper.INSTANCE.toModuleDto(module);
		Assert.assertNotNull(result);
		assertEquals(exception, ((ResultModuleAction) result).getCause());
		assertEquals(ModuleAction.State.PROGRESS, result.getState());
		assertEquals(moduleDto, result.getModule());
		verify(module, times(1)).healthGoDown(eq(exception));
		verify(actionMetricsContainer, times(2)).changed(any(ModuleAction.class));
		verify(actionMetricsContainer, times(1)).fail(any(ModuleAction.class), eq(exception));
	}

	@Test(expected = ModuleActionRuntimeException.class)
	public void testExecuteAtomicModuleActionBadRethrow() {
		factory.setUp();
		RuntimeException exception = new RuntimeException("test");
		Runnable throwException = () -> {
			throw exception;
		};
		factory.executeAtomicModuleAction(module, "test", throwException, true);
		Assert.fail("Here I was waiting for exception.");
	}

	@Test
	public void testCreateActionContext() throws Exception {
		ActionContext<Long, Long> context = factory.createContext(0L, () -> 1L);

		assertNull(context.getOutput());
		context.addCriteria("test", 100L);
		context.saveResult(context.getAction().call());

		assertEquals(0L, context.getInput().longValue());
		assertEquals(1L, context.getOutput().longValue());
		assertEquals(1L, context.getAction().call().longValue());
		assertNull(context.getCriteria().get("Test"));
		assertEquals(new Long(100), context.getCriteria().get("test"));
	}

	@Test
	public void testCreateAndOperateAtomicModuleActionHappyDayScenario() {
		ActionContext<Long, Long> context = spy(factory.createContext(0L, () -> 1L));

		String actionName = "test";
		AtomicModuleAction action = factory.createAtomicModuleAction(module, actionName, context, true);
		ModuleAction result = action.operate();

		assertNotNull(result);
		assertNull(((ResultModuleAction) result).getCause());
		assertEquals(0L, context.getInput().longValue());
		assertEquals(1L, context.getOutput().longValue());

		verify(factory, times(1)).createModuleRegularAction(eq(module), eq(actionName));
		verify(actionStorage, times(1)).createActionFor(eq(module), any(ModuleAction.class), eq(actionName));
		verify(actionMetricsContainer, times(2)).changed(any(ModuleAction.class));
		verify(actionMetricsContainer, times(1)).start(any(ModuleAction.class), eq(context));
		verify(actionMetricsContainer, times(1)).finish(any(ModuleAction.class), eq(1L));
		verify(actionMetricsContainer, times(1)).success(any(ModuleAction.class));
		verify(context, times(1)).getAction();
		verify(context, times(1)).saveResult(eq(1L));
		verify(module, times(1)).healthGoUp();
		verify(factory, times(3)).scheduleStorage(any(ModuleAction.class));
	}

	@Test
	public void testCreateAndOperateAtomicModuleActionManagedExceptionScenario() {
		RuntimeException exception = new RuntimeException("test-exception");
		ActionContext<Long, Long> context = spy(factory.createContext(0L, () -> {
			throw exception;
		}));

		String actionName = "test";
		AtomicModuleAction action = factory.createAtomicModuleAction(module, actionName, context, false);
		ModuleAction result = action.operate();

		assertNotNull(result);
		assertEquals(exception, ((ResultModuleAction) result).getCause());
		assertEquals(0L, context.getInput().longValue());
		assertNull(context.getOutput());

		verify(factory, times(1)).createModuleRegularAction(eq(module), eq(actionName));
		verify(actionStorage, times(1)).createActionFor(eq(module), any(ModuleAction.class), eq(actionName));
		verify(actionMetricsContainer, times(2)).changed(any(ModuleAction.class));
		verify(actionMetricsContainer, times(1)).start(any(ModuleAction.class), eq(context));
		verify(actionMetricsContainer, times(1)).fail(any(ModuleAction.class), eq(exception));
		verify(context, times(1)).getAction();
		verify(module, times(1)).healthGoDown(eq(exception));
		verify(factory, times(3)).scheduleStorage(any(ModuleAction.class));
	}

	@Test(expected = ModuleActionRuntimeException.class)
	public void testCreateAndOperateAtomicModuleActionNonManagedExceptionScenario() {
		RuntimeException exception = new RuntimeException("test-exception");
		ActionContext<Long, Long> context = spy(factory.createContext(0L, () -> {
			throw exception;
		}));

		String actionName = "test";
		AtomicModuleAction action = factory.createAtomicModuleAction(module, actionName, context, true);
		action.operate();
		fail("Here we were waiting for rethrown exception.");
	}

	@Test
	public void testCurrentAction() {
		ThreadLocal<ModuleAction> actions = factory.current;
		Assert.assertNull(factory.currentAction());
		ModuleAction action = mock(ModuleAction.class);
		actions.set(action);
		assertEquals(action, factory.currentAction());
	}

	@Test
	public void testStartMainAction() {
		ModuleAction action = mock(ModuleAction.class);
		when(module.getMainAction()).thenReturn(action);
		when(action.getModule()).thenReturn(module);

		factory.startMainAction(module);
		assertEquals(action, factory.currentAction());
		verify(actionMetricsContainer, times(0)).changed(eq(action));

		when(action.getState()).thenReturn(ModuleAction.State.PROGRESS);
		factory.startMainAction(module);
		verify(actionMetricsContainer, times(1)).changed(eq(action));
	}

	@Test
	public void testFinishMainAction() {
		ModuleAction action = mock(ModuleAction.class);
		when(module.getMainAction()).thenReturn(action);
		when(action.getModule()).thenReturn(module);

		factory.current.set(action);

		factory.finishMainAction(module, true);
		Assert.assertNull(factory.currentAction());
		verify(actionMetricsContainer, times(1)).success(eq(action));

		RuntimeException exception = new RuntimeException("test");
		when(module.lastThrown()).thenReturn(exception);
		factory.current.set(action);

		factory.finishMainAction(module, false);
		Assert.assertNull(factory.currentAction());
		verify(actionMetricsContainer, times(1)).fail(eq(action), eq(exception));
	}

	// private methods
	private void prepareActionStorage() {
		when(actionStorage.createActionFor(any(Module.class)))
				.thenAnswer((Answer<ModuleAction>) invocation -> {
					ModuleBasics module = (ModuleBasics) invocation.getArguments()[0];
					ModuleActionAdapter result = ActionMapper.INSTANCE.simple(module, "main-test");
					result.setModule(DtoMapper.INSTANCE.toModuleDto(module));
					return result;
				});
		when(actionStorage.createActionFor(any(Module.class), any(ModuleAction.class), anyString())).thenAnswer((Answer<ModuleAction>) invocation -> {
			ModuleBasics module = (ModuleBasics) invocation.getArguments()[0];
			ModuleActionAdapter result = ActionMapper.INSTANCE.simple(module, "regular-test");
			result.setModule(DtoMapper.INSTANCE.toModuleDto(module));
			result.setParent((ModuleAction) invocation.getArguments()[1]);
			result.setName((String) invocation.getArguments()[2]);
			return result;
		});
	}

}