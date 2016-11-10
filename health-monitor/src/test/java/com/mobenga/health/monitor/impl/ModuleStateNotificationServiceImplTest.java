package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.transport.ModuleWrapper;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.storage.HeartBeatStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutorService;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Test ModuleStateNotificationServiceImpl
 * @see ModuleStateNotificationServiceImpl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:com/mobenga/health/monitor/factory/impl/test-basic-monitor-services.xml",
        "classpath:com/mobenga/health/monitor/impl/test-module-state-notification.xml",
        "classpath:com/mobenga/health/monitor/storage/test-monitor-stubs.xml"
})
public class ModuleStateNotificationServiceImplTest {

    @Autowired
    private ModuleStateNotificationServiceImpl service;

    @Autowired
    private HeartBeatStorage storage;

    @Autowired
    private ModuleConfigurationService configuration;

    @Autowired
    private ModuleActionMonitorServiceImpl actionService;

    @Autowired
    @Qualifier("serviceRunner")
    private ExecutorService executor;

    private MonitoredService state = mockState();

    @Before
    public void beforeTestService() throws Exception {
        actionService.initialize();
        service.stopService();
        service.unregisterAll();
        service.setHeartbeatDelay(10);
        service.startService();
        service.unRegister(service);
        reset(configuration, storage);
    }

    @Test
    public void testRegister() throws Exception {

        final Object semaphore = new Object();
        final Answer signal = new signal(semaphore);
        doAnswer(signal).when(storage).saveHeartBeat(state);

        service.register(state);

        synchronized (semaphore){
            semaphore.wait(100);
        }
        verify(storage, atLeastOnce()).saveHeartBeat(eq(state));
        verify(configuration, atLeastOnce()).getUpdatedVariables(eq(state.getModulePK()),any());
    }

    @Test
    public void testUnRegister() throws Exception {
        final Object semaphore = new Object();
        final Answer signal = new signal(semaphore);

        doAnswer(signal).when(storage).saveHeartBeat(state);

        service.register(state);

        synchronized (semaphore){
            semaphore.wait(1000);
        }
        verify(storage, atLeastOnce()).saveHeartBeat(eq(state));
        verify(configuration, atLeastOnce()).getUpdatedVariables(any(HealthItemPK.class),any());
        reset(storage, configuration);
        // unregister
        service.unRegister(state);

        doAnswer(signal).when(storage).saveHeartBeat(state);
        synchronized (semaphore){
            semaphore.wait(1000);
        }
        verify(storage, never()).saveHeartBeat(eq(state));
        verify(configuration, never()).getUpdatedVariables(eq(state.getModulePK()),any());
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
        final ModuleWrapper module = new ModuleWrapper();
        module.setDescription("descr");
        module.setSystemId("sys-id");
        module.setApplicationId("app-id");
        module.setVersionId("ver-id");
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