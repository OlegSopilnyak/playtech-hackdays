package com.mobenga.health.monitor.impl;

import static com.mobenga.health.HealthUtils.key;
import com.mobenga.health.model.ModulePK;
import com.mobenga.health.model.transport.ModuleWrapper;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.storage.HealthModuleStorage;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

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
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
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
        ModulePK expResult = new ModuleWrapper(instance);
        ModulePK result = instance.getModulePK();
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
        ModulePK expResult = new ModuleWrapper(instance.getModulePK());
        String moduleId = key(instance.getModule(instance.getModulePK()));
        ModulePK result = instance.getModule(moduleId);
        assertEquals(expResult, result);
    }

}
