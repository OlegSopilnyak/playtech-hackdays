package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.transport.ModuleKeyDto;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.storage.HealthModuleStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.mobenga.health.HealthUtils.key;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Testing modules manager
 */
@RunWith(MockitoJUnitRunner.class)
public class HealthModuleServiceImplTest {

    @InjectMocks
    HealthModuleServiceImpl instance = new HealthModuleServiceImpl();
    @Mock
    private DistributedContainersService distributed;
    @Mock
    private HealthModuleStorage storage;
    @Spy
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    @Mock
    private ModuleStateNotificationService notifier;

    @Before
    public void setUp() {
        when(distributed.map(anyString())).thenReturn(new HashMap());
        when(distributed.queue(anyString())).thenReturn(new ArrayBlockingQueue(100));
        instance.initialize();
    }

    @After
    public void tearDown() {
        instance.shutdown();
    }

    @Test
    public void testGetModulePK() {
        ModuleKey expResult = new ModuleKeyDto(instance);
        ModuleKey result = instance;
        assertEquals(expResult, result);
    }

    @Test
    public void testInitialize() {

        
        instance.initialize();

        assertEquals(true, instance.isActive());
    }

    @Test
    public void testShutdown() {
        instance.shutdown();
        assertEquals(false, instance.isActive());
    }

    @Test
    public void testGetModule_String() {
        ModuleKey expResult = new ModuleKeyDto(instance);
        String moduleId = key(instance.getModule(instance));
        ModuleKey result = instance.getModule(moduleId);
        assertEquals(expResult, result);
    }

}
