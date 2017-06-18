package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;
import com.mobenga.health.model.ModulePK;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Testing behavior of ModuleActionMonitorServiceImp
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ModuleActionMonitorServiceImplTest {


    @Mock
    private MonitoredActionStorage storage;
    @Mock
    private DistributedContainersService distributed;
    @Mock
    private ModuleStateNotificationService notifier;
    @Spy
    private final ExecutorService executor = Executors.newFixedThreadPool(1);


    private final MonitoredAction action = new MonitoredActionStub();
    @Mock
    private ModulePK module;

    @InjectMocks
    private final ModuleActionMonitorServiceImpl service = new ModuleActionMonitorServiceImpl();

    @Before
    public void prepareService(){
        when(distributed.queue(anyString())).thenReturn(new ArrayBlockingQueue(10));
        when(storage.createMonitoredAction()).thenReturn(action);
        service.initialize();
    }
    @After
    public void unPrepareService(){
        reset(storage);
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
        service.actionMonitoring(module, action);
        Thread.sleep(1000);
        verify(storage, atLeastOnce()).saveActionState(any(ModulePK.class), eq(action));

    }
}