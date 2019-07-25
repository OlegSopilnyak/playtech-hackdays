/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module.model.action;

import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.action.bean.result.FailModuleAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class FailModuleActionTest {
    private FailModuleAction action;
    private Throwable cause = new Exception();
    private ModuleAction actionContext = mock(ModuleAction.class);
    private ModuleBasics module = mock(ModuleBasics.class);
    
    
    @Before
    public void setUp() {
        when(actionContext.getId()).thenReturn("0");
        when(actionContext.getDescription()).thenReturn("1");
        when(actionContext.getDuration()).thenReturn(2L);
        when(actionContext.getHostName()).thenReturn("3");
        when(actionContext.getName()).thenReturn("4");
        when(actionContext.getStarted()).thenReturn(Instant.MAX);
        when(actionContext.getState()).thenReturn(ModuleAction.State.FAIL);
        when(actionContext.getModule()).thenReturn(module);
        action = new FailModuleAction(actionContext, cause);
    }

    @After
    public void tearDown() {
        reset(actionContext);
    }

    @Test
    public void testSomeMethod() {
        assertEquals("0", action.getId());
        assertEquals("1", action.getDescription());
        assertEquals(2L, action.getDuration().longValue());
        assertEquals("3", action.getHostName());
        assertEquals("4", action.getName());
        assertEquals(Instant.MAX, action.getStarted());
        assertEquals(ModuleAction.State.FAIL, action.getState());
        assertEquals(module, action.getModule());
        assertEquals(action.getCause(), cause);
    }
    
}
