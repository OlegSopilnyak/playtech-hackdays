package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.storage.HeartBeatStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.UnknownHostException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Test ModuleStateNotificationServiceImpl
 * @see ModuleStateNotificationServiceImpl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:com/mobenga/health/monitor/impl/test-module-state-notification.xml"})
public class ModuleStateNotificationServiceImplTest {

    @Autowired
    private ModuleStateNotificationServiceImpl service;

    @Autowired
    private HeartBeatStorage storage;

    @Autowired
    private ModuleConfigurationService configuration;

    private MonitoredService state = mockState();

    @Before
    public void beforeTestService() throws Exception {
        service.stopService();
        service.unregisterAll();
        service.startService();
    }

    @Test
    public void testRegister() throws Exception {

        reset(storage, configuration);

        final Object semaphore = new Object();
        final Answer signal = new signal(semaphore);
        doAnswer(signal).when(storage).saveHeartBeat(state);

        service.unRegister(service);
        service.register(state);

        synchronized (semaphore){
            semaphore.wait(100);
        }
        verify(storage, atLeastOnce()).saveHeartBeat(eq(state));
        verify(configuration, atLeastOnce()).getUpdatedVariables(eq(state.getModulePK()),any());
    }

    @Test
    public void testUnRegister() throws Exception {
        reset(storage, configuration);
        final Object semaphore = new Object();
        final Answer signal = new signal(semaphore);

        doAnswer(signal).when(storage).saveHeartBeat(state);

        service.unRegister(service);
        service.register(state);

        synchronized (semaphore){
            semaphore.wait(100);
        }
        verify(storage, atLeastOnce()).saveHeartBeat(eq(state));
        verify(configuration, atLeastOnce()).getUpdatedVariables(eq(state.getModulePK()),any());
        reset(storage, configuration);
        // unregister
        service.unRegister(state);
        doAnswer(signal).when(storage).saveHeartBeat(state);
        synchronized (semaphore){
            semaphore.wait(100);
        }
        verify(storage, times(0)).saveHeartBeat(state);
        verify(configuration, times(0)).getUpdatedVariables(eq(state.getModulePK()),any());
    }

    @Test
    public void testStartService() throws Exception {

        service.register(state);

        service.stopService();
        assertFalse(service.isActive());
        service.startService();
        assertTrue(service.isActive());
    }

    @Test
    public void testStopService() throws Exception {
        service.register(state);

        assertTrue(service.isActive());
        service.stopService();
        assertFalse(service.isActive());
        service.startService();
    }

    @Test
    public void testIsActive() throws Exception {
        assertTrue(service.isActive());
        service.stopService();
        assertFalse(service.isActive());
    }

    @Test
    public void testCheckHealth() throws Exception {

        service.checkHealth(state);

        verify(storage, times(1)).saveHeartBeat(eq(state));
        verify(configuration, times(1)).getUpdatedVariables(eq(state.getModulePK()),any());
    }

    @NotNull
    private MonitoredService mockState() {
        final MonitoredService state = mock(MonitoredService.class);
        final HealthItemPK module = mock(HealthItemPK.class);
        when(state.getModulePK()).thenReturn(module);
        return state;
    }

    private static class signal implements Answer{
        final Object semaphore;
        signal(Object semaphore){
            this.semaphore = semaphore;
        }
        @Override
        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
            synchronized (semaphore){
                semaphore.notify();
            }
            return null;
        }
    }
}