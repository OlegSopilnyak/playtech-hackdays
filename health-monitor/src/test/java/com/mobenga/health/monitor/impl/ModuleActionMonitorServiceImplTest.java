package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.MonitoredAction;
import com.mobenga.health.model.transport.MonitoredActionDto;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

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
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);


    private final MonitoredAction action = new MonitoredActionDto();
    @Mock
    private ModuleKey module;

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
        verify(storage, atLeastOnce()).saveActionState(any(ModuleKey.class), eq(action));

    }
}