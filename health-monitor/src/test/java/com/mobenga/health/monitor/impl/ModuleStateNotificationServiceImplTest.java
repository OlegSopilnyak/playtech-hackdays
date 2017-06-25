package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.health.model.factory.impl.TimeServiceImpl;
import com.mobenga.health.model.factory.impl.UniqueIdGeneratorImpl;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.model.transport.ModuleWrapper;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.storage.HeartBeatStorage;
import com.mobenga.health.storage.ModuleOutputStorage;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test ModuleStateNotificationServiceImpl
 * @see ModuleStateNotificationService
 */
@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder
public class ModuleStateNotificationServiceImplTest {

//    @Spy
    @InjectMocks
    private final ModuleStateNotificationServiceImpl service = new ModuleStateNotificationServiceImpl();
    
    @InjectMocks
    private final LogModuleServiceImpl loggerService = new LogModuleServiceImpl();
    
    @Spy
    private final ExecutorService executor = Executors.newFixedThreadPool(20);
    @Mock
    private DistributedContainersService distributed;  
    @Spy
    private final TimeService timeService = new TimeServiceImpl();
    @Spy
    private final UniqueIdGenerator idGenerator = new UniqueIdGeneratorImpl();

    
    @Mock
    private ModuleOutputStorage loggerStorage;
    @Mock
    private MonitoredActionStorage actionStorage;

    @Mock
    private HeartBeatStorage storage;

    @Mock
    private ModuleConfigurationService configuration;

    private final ModuleWrapper module = new ModuleWrapper();
    private final MonitoredService state = mockState();

    public ModuleStateNotificationServiceImplTest() {
    }

    @Before
    public void prepareService() throws Exception {
        loggerService.setNotifier(service);
        service.setHeartbeatDelay(10);
        
        when(distributed.map(anyString())).thenReturn(new HashMap());
        when(distributed.queue(anyString())).thenReturn(new ArrayBlockingQueue(100));
        
        MonitoredActionStub action = new MonitoredActionStub();
        action.setId("890");
        action.setState(MonitoredAction.State.INIT);
        when(actionStorage.createMonitoredAction()).thenReturn(action);
        
        service.startService();
        service.unRegister(service);
        loggerService.startService();
    }
    
    @After
    public void unPrepareService(){
        service.stopService();
        loggerService.stopService();
        reset(configuration, storage, actionStorage);
    }

    @Test
    public void testRegister() throws Exception {

        final Object semaphore = new Object();
        Map<String, ConfiguredVariableItem> updatedConfiguration = new HashMap<>();
        updatedConfiguration.put("1.1.1.1.1.none", new LocalConfiguredVariableItem("none","Testing variable","Hello"));

        when(configuration.getUpdatedVariables(eq(state.getModulePK()), any(Map.class))).then(new Answer<Map>() {
            @Override
            public Map answer(InvocationOnMock invocationOnMock) throws Throwable {
                synchronized (semaphore){
                    semaphore.notify();
                }
                return updatedConfiguration;
            }
        });


        service.register(state);

        synchronized (semaphore){
            semaphore.wait(300);
        }
        verify(storage, atLeastOnce()).saveHeartBeat(eq(state));
        verify(configuration, atLeastOnce()).getUpdatedVariables(eq(state.getModulePK()),any());
        verify(state, atLeastOnce()).configurationChanged(eq(updatedConfiguration));
    }

    @Test
    public void testUnRegister() throws Exception {
        final Object semaphore = new Object();

        Map<String, ConfiguredVariableItem> updatedConfiguration = new HashMap<>();
        updatedConfiguration.put("1.1.1.1.1.none", new LocalConfiguredVariableItem("none","Testing variable","Hello"));

        when(configuration.getUpdatedVariables(eq(state.getModulePK()), any(Map.class))).then(new Answer<Map>() {
            @Override
            public Map answer(InvocationOnMock invocationOnMock) throws Throwable {
                synchronized (semaphore){
                    semaphore.notify();
                }
                return updatedConfiguration;
            }
        });

        // register the mock-state
        service.register(state);

        synchronized (semaphore){
            semaphore.wait(300);
        }
        verify(storage, atLeastOnce()).saveHeartBeat(eq(state));
        verify(configuration, atLeastOnce()).getUpdatedVariables(eq(state.getModulePK()),any());
        verify(state, atLeastOnce()).configurationChanged(eq(updatedConfiguration));
        // unregister mock-state
        service.unRegister(state);

        // reset all mocks
        reset(storage, configuration, state);
        when(state.getModulePK()).thenReturn(module);
        when(configuration.getUpdatedVariables(eq(state.getModulePK()), any(Map.class))).then(new Answer<Map>() {
            @Override
            public Map answer(InvocationOnMock invocationOnMock) throws Throwable {
                synchronized (semaphore){
                    semaphore.notify();
                }
                return updatedConfiguration;
            }
        });

        // unregister
        service.unRegister(state);

        synchronized (semaphore){
            semaphore.wait(300);
        }
        verify(storage, never()).saveHeartBeat(eq(state));
        verify(configuration, never()).getUpdatedVariables(eq(state.getModulePK()),any());
        verify(state, never()).configurationChanged(eq(updatedConfiguration));
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