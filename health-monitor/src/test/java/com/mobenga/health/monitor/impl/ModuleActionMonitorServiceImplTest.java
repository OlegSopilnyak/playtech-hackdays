package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

/**
 * Testing behavior of ModuleActionMonitorServiceImp
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:com/mobenga/health/monitor/impl/test-module-action-monitor.xml",
        "classpath:com/mobenga/health/monitor/factory/impl/test-basic-monitor-services.xml"
})
public class ModuleActionMonitorServiceImplTest {

    @Autowired
    private ModuleActionMonitorServiceImpl service;

    @Autowired
    private MonitoredActionStorage storage;


    @Autowired
    private MonitoredAction action;

    @Autowired
    private HealthItemPK module;

    @Before
    public void prepareService(){
        when(storage.createMonitoredAction()).thenReturn(action);
        service.initialize();
    }
    @After
    public void unPrepareService(){
        service.shutdown();
    }

    @Test
    public void testCreateMonitoredAction() throws Exception {
        MonitoredAction created = service.createMonitoredAction();
        assertFalse(action == created);
        assertEquals(action, created);
    }

    @Test
    public void testActionMonitoring() throws Exception {
        service.actionMonitoring(module ,action);
        Thread.sleep(1000);
        verify(storage, atLeastOnce()).saveActionState(any(HealthItemPK.class), eq(action));

    }
}