/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.impl;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.action.ModuleActionAdapter;
import oleg.sopilnyak.service.action.exception.ModuleActionRuntimeException;
import oleg.sopilnyak.service.action.result.FailModuleAction;
import oleg.sopilnyak.service.action.result.ResultModuleAction;
import oleg.sopilnyak.service.action.result.SuccessModuleAction;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
	private ModuleActionStorage actionStorage;
	@Mock
	private ObjectProvider<SuccessModuleAction> successActions;
	@Mock
	private ObjectProvider<FailModuleAction> failedActions;

	@Spy
	private ScheduledExecutorService runner = new ScheduledThreadPoolExecutor(2);

	@InjectMocks
	private ModuleActionFactoryImpl factory = new ModuleActionFactoryImpl();

	@Before
	public void setUp() throws Exception {
		when(module.getSystemId()).thenReturn("sys-test");
		when(module.getModuleId()).thenReturn("mod-test");
		when(module.getDescription()).thenReturn("desc-test");
		when(module.primaryKey()).thenReturn("test-module-primary-key");

		when(module.getMetricsContainer()).thenReturn(metricsContainer);
		when(metricsContainer.action()).thenReturn(actionMetricsContainer);

		prepareActionStorage();

		factory.delay = 200L;
	}

	@After
	public void tearDown() {
		reset(module, metricsContainer, actionMetricsContainer);
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
		ModuleDto moduleDto = new ModuleDto(module);
		Assert.assertEquals(moduleDto, action.getModule());
		Assert.assertEquals(ModuleAction.State.INIT, action.getState());
		Assert.assertTrue(StringUtils.isEmpty(action.getId()));
		verify(actionMetricsContainer, times(1)).changed(eq(action));
	}

	@Test
	public void testCreateModuleRegularAction() {
		factory.setUp();
		ModuleAction action = factory.createModuleRegularAction(module, "test");
		ModuleDto moduleDto = new ModuleDto(module);
		Assert.assertEquals(moduleDto, action.getModule());
		Assert.assertEquals(ModuleAction.State.INIT, action.getState());
		Assert.assertTrue(StringUtils.isEmpty(action.getId()));
		verify(actionMetricsContainer, times(1)).changed(eq(action));
	}

	@Test
	public void testExecuteAtomicModuleActionGood() {
		factory.setUp();
		AtomicInteger value = new AtomicInteger(0);
		reset(actionMetricsContainer);

		ModuleAction result = factory.executeAtomicModuleAction(module, "test", () -> value.getAndSet(100), true);

		ModuleDto moduleDto = new ModuleDto(module);
		Assert.assertNotNull(result);
		Assert.assertNull(((ResultModuleAction) result).getCause());
		Assert.assertEquals(ModuleAction.State.PROGRESS, result.getState());
		Assert.assertEquals(moduleDto, result.getModule());
		Assert.assertEquals(100, value.get());
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

		ModuleDto moduleDto = new ModuleDto(module);
		Assert.assertNotNull(result);
		Assert.assertEquals(exception, ((ResultModuleAction) result).getCause());
		Assert.assertEquals(ModuleAction.State.PROGRESS, result.getState());
		Assert.assertEquals(moduleDto, result.getModule());
		verify(module, times(1)).healthGoLow(eq(exception));
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
	public void testCurrentAction() {
		ThreadLocal<ModuleAction> actions = factory.current;
		Assert.assertNull(factory.currentAction());
		ModuleAction action = mock(ModuleAction.class);
		actions.set(action);
		Assert.assertEquals(action, factory.currentAction());
	}

	@Test
	public void testStartMainAction() {
		ModuleAction action = mock(ModuleAction.class);
		when(module.getMainAction()).thenReturn(action);
		when(action.getModule()).thenReturn(module);

		factory.startMainAction(module);
		Assert.assertEquals(action, factory.currentAction());
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
		when(successActions.getObject(any(ModuleAction.class)))
				.thenAnswer((Answer<SuccessModuleAction>) invocation -> new SuccessModuleAction((ModuleAction) invocation.getArguments()[0]));
		when(failedActions.getObject(any(ModuleAction.class), any(Throwable.class)))
				.thenAnswer((Answer<FailModuleAction>) invocation -> new FailModuleAction((ModuleAction) invocation.getArguments()[0], (Throwable) invocation.getArguments()[1]));
		when(actionStorage.createActionFor(any(Module.class)))
				.thenAnswer((Answer<ModuleAction>) invocation -> new ModuleActionAdapter((ModuleBasics) invocation.getArguments()[0], "main-test"));
		when(actionStorage.createActionFor(any(Module.class), any(ModuleAction.class), anyString())).thenAnswer((Answer<ModuleAction>) invocation -> {
			ModuleActionAdapter result1 = new ModuleActionAdapter((ModuleBasics) invocation.getArguments()[0], "regular-test");
			result1.setParent((ModuleAction) invocation.getArguments()[1]);
			result1.setName((String) invocation.getArguments()[2]);
			return result1;
		});
	}

}