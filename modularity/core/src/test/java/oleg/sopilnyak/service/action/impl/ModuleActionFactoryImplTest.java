/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.impl;

import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.ModuleActionRuntimeException;
import oleg.sopilnyak.module.model.action.ResultModuleAction;
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.dto.ModuleDto;
import oleg.sopilnyak.service.metric.ActionMetricsContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
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
    private String hostName;

    @Spy
    private UniqueIdGenerator generator = new ModuleUtilityConfiguration().getUniqueIdGenerator();

    @InjectMocks
    private ModuleActionFactoryImpl factory = new ModuleActionFactoryImpl();

    @Before
    public void setUp() throws Exception {
        hostName = InetAddress.getLocalHost().getHostName();
        when(module.getSystemId()).thenReturn("sys-test");
        when(module.getModuleId()).thenReturn("mod-test");
        when(module.getDescription()).thenReturn("desc-test");
        when(module.getMetricsContainer()).thenReturn(metricsContainer);
        when(metricsContainer.action()).thenReturn(actionMetricsContainer);
    }

    @After
    public void tearDown() {
        reset(module, metricsContainer, actionMetricsContainer);
    }

    @Test
    public void testSetUp() {
        factory.setUp();
        String host = (String) ReflectionTestUtils.getField(factory, "hostName");
        assertNotEquals("localhost",host);
        assertEquals(hostName, host);
    }

    @Test
    public void testCreateModuleMainAction() {
        factory.setUp();
        ModuleAction action = factory.createModuleMainAction(module);
        ModuleDto moduleDto = new ModuleDto(module);
        assertEquals(moduleDto, action.getModule());
        assertEquals(ModuleAction.State.INIT, action.getState());
        assertEquals(hostName, action.getHostName());
        assertTrue(action.getDescription().startsWith("Main"));
        assertFalse(StringUtils.isEmpty(action.getId()));
        verify(actionMetricsContainer, times(1)).changed(eq(action));
    }

    @Test
    public void testCreateModuleRegularAction() {
        factory.setUp();
        ModuleAction action = factory.createModuleRegularAction(module, "test");
        ModuleDto moduleDto = new ModuleDto(module);
        assertEquals(moduleDto, action.getModule());
        assertEquals(ModuleAction.State.INIT, action.getState());
        assertEquals("[test-action]", action.getName());
        assertTrue(action.getDescription().startsWith("test action"));
        assertEquals(hostName, action.getHostName());
        assertFalse(StringUtils.isEmpty(action.getId()));
        verify(actionMetricsContainer, times(1)).changed(eq(action));
    }

    @Test
    public void testExecuteAtomicModuleActionGood() {
        factory.setUp();
        AtomicInteger value = new AtomicInteger(0);
        ModuleAction result = factory.executeAtomicModuleAction(module, "test", ()->value.getAndSet(100), true);
        ModuleDto moduleDto = new ModuleDto(module);
        assertNotNull(result);
        assertNull(((ResultModuleAction)result).getCause());
        assertEquals(ModuleAction.State.PROGRESS, result.getState());
        assertEquals(moduleDto, result.getModule());
        assertEquals("[test-action]", result.getName());
        assertTrue(result.getDescription().startsWith("test action"));
        assertEquals(hostName, result.getHostName());
        assertEquals(100, value.get());
        verify(module, times(1)).healthGoUp();
        verify(actionMetricsContainer, times(2)).changed(eq(result));
        verify(actionMetricsContainer, times(1)).success(eq(result));
    }

    @Test
    public void testExecuteAtomicModuleActionBadNoRethrow() {
        factory.setUp();
        RuntimeException exception = new RuntimeException("test");
        Runnable throwException = ()->{throw exception;};
        ModuleAction result = factory.executeAtomicModuleAction(module, "test", throwException, false);
        ModuleDto moduleDto = new ModuleDto(module);
        assertNotNull(result);
        assertEquals(exception, ((ResultModuleAction)result).getCause());
        assertEquals(ModuleAction.State.PROGRESS, result.getState());
        assertEquals(moduleDto, result.getModule());
        assertEquals("[test-action]", result.getName());
        assertTrue(result.getDescription().startsWith("test action"));
        assertEquals(hostName, result.getHostName());
        verify(module, times(1)).healthGoLow(eq(exception));
        verify(actionMetricsContainer, times(2)).changed(eq(result));
        verify(actionMetricsContainer, times(1)).fail(eq(result), eq(exception));
    }

    @Test(expected = ModuleActionRuntimeException.class)
    public void testExecuteAtomicModuleActionBadRethrow() {
        factory.setUp();
        RuntimeException exception = new RuntimeException("test");
        Runnable throwException = ()->{throw exception;};
        factory.executeAtomicModuleAction(module, "test", throwException, true);
        fail("Here I was waiting for exception.");
    }

    @Test
    public void testCurrentAction() {
        ThreadLocal<ModuleAction> actions = (ThreadLocal<ModuleAction>) ReflectionTestUtils.getField(factory, "current");
        assertNull(factory.currentAction());
        ModuleAction action = mock(ModuleAction.class);
        actions.set(action);
        assertEquals(action, factory.currentAction());
    }

    @Test
    public void testStartMainAction() {
        ModuleAction action = mock(ModuleAction.class);
        when(module.getMainAction()).thenReturn(action);

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
        ThreadLocal<ModuleAction> actions = (ThreadLocal<ModuleAction>) ReflectionTestUtils.getField(factory, "current");

        actions.set(action);
        factory.finishMainAction(module, true);
        assertNull(factory.currentAction());
        verify(actionMetricsContainer, times(1)).success(eq(action));

        RuntimeException exception = new RuntimeException("test");
        when(module.lastThrown()).thenReturn(exception);
        actions.set(action);
        factory.finishMainAction(module, false);
        assertNull(factory.currentAction());
        verify(actionMetricsContainer, times(1)).fail(eq(action), eq(exception));
    }
}