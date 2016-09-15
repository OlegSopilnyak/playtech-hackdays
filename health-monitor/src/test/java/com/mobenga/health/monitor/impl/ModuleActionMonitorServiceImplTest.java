package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.storage.MonitoredActionStorage;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.*;

/**
 * Testing behavior of ModuleActionMonitorServiceImp
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:com/mobenga/health/monitor/impl/test-module-action-monitor.xml"})
public class ModuleActionMonitorServiceImplTest extends TestCase {

    @Autowired
    private ModuleActionMonitorServiceImpl service;

    @Autowired
    private MonitoredActionStorage storage;

    @Autowired
    private MonitoredAction action;

    @Autowired
    private HealthItemPK module;

    @Test
    public void testCreateMonitoredAction() throws Exception {
        when(storage.createMonitoredAction()).thenReturn(action);
        MonitoredAction entity = service.createMonitoredAction();
        assertEquals(action, entity);
    }

    @Test
    public void testActionMonitoring() throws Exception {
        service.actionMonitoring(module ,action);
        verify(storage, atLeastOnce()).saveActionState(module, action);

    }
}