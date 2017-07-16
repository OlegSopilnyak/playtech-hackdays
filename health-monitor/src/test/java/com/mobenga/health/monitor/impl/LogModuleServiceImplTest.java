package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.MonitoredAction;
import com.mobenga.health.model.business.out.ModuleOutputDevice;
import com.mobenga.health.model.business.out.log.ModuleLoggerMessage;
import com.mobenga.health.model.transport.ConfiguredVariableItemDto;
import com.mobenga.health.model.transport.ModuleKeyDto;
import com.mobenga.health.model.transport.MonitoredActionDto;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.TimeService;
import com.mobenga.health.monitor.UniqueIdGenerator;
import com.mobenga.health.storage.ModuleOutputStorage;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.mobenga.health.HealthUtils.key;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for log module-output service
 */
@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder
public class LogModuleServiceImplTest {

    @InjectMocks
    private final LogModuleServiceImpl service = new LogModuleServiceImpl();

    @Spy
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    @Mock
    private DistributedContainersService distributed;  
    @Mock
    private ModuleStateNotificationService notifier;
    @Spy
    private final TimeService timeService = new TimeServiceImpl();
    @Spy
    private final UniqueIdGenerator idGenerator = new UniqueIdGeneratorImpl();

    
    private final BlockingQueue distributedQueue = new ArrayBlockingQueue(200);
    @Mock
    private ModuleOutputStorage storage;
    @Mock
    private MonitoredActionStorage actionStorage;

    @Before
    public void prepareService(){
        when(distributed.map(anyString())).thenReturn(new HashMap());
        when(distributed.queue(anyString())).thenReturn(distributedQueue);
        
        MonitoredActionDto action = new MonitoredActionDto();
        action.setId("890");
        action.setState(MonitoredAction.State.INIT);
        when(actionStorage.createMonitoredAction()).thenReturn(action);
        
        
        service.startService();
        
    }
    @After
    public void unPrepareService() throws InterruptedException{
        service.stopService();
        reset(storage, actionStorage);
    }
    
    @Test
    public void startService() throws Exception {
        service.stopService();
        service.startService();
        assertFalse(!service.isActive());
    }

    @Test
    public void stopService() throws Exception {
        service.stopService();
        assertFalse(service.isActive());
    }

    @Test
    @Ignore
    public void create() throws Exception {
        final String system = "mockSys-c",
                application = "mockApp-c",
                version = "mockVer-c",
                description = "mockDescription-c";

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        final ModuleLoggerMessage output = mock(ModuleLoggerMessage.class);
        when(storage.createModuleOutput(pk, ModuleLoggerMessage.LOG_OUTPUT_TYPE)).thenReturn(output);

        ModuleOutputDevice device = service.create(pk);

        assertNotNull(device);

        device.out("Hello world");
        
        Thread.sleep(500);
        
        verify(storage, times(1)).createModuleOutput(any(ModuleKey.class), eq(ModuleLoggerMessage.LOG_OUTPUT_TYPE));
        verify(output, times(1)).setId(anyString());
        verify(output, times(1)).setPayload(anyString());
    }

    @Test
    public void deviceOut() throws Exception {
        final String system = "mockSys-1",
                application = "mockApp-1",
                version = "mockVer-1",
                description = "mockDescription1";

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        pk = new ModuleKeyDto(pk);

        final ModuleLoggerMessage output = mock(ModuleLoggerMessage.class);
        when(storage.createModuleOutput(eq(pk), eq(ModuleLoggerMessage.LOG_OUTPUT_TYPE))).thenReturn(output);

        ModuleOutputDevice device = service.create(pk);
        assertNotNull(device);

        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");

        Thread.sleep(500);
        
        verify(storage, times(4)).createModuleOutput(eq(pk), eq(ModuleLoggerMessage.LOG_OUTPUT_TYPE));
        verify(output, times(4)).setId(any());
        verify(output, times(4)).setActionId(anyString());
        verify(output, times(4)).setPayload(eq("Hello world"));
        verify(storage, times(4)).saveModuleOutput(eq(output));
    }

    @Test
    public void deviceActionOutSuccess() throws Exception {
        final String system = "mockSys-2",
                application = "mockApp-2",
                version = "mockVer-2",
                description = "mockDescription-2";

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        pk = new ModuleKeyDto(pk);

        final ModuleLoggerMessage output = mock(ModuleLoggerMessage.class);
        when(storage.createModuleOutput(eq(pk), eq(ModuleLoggerMessage.LOG_OUTPUT_TYPE))).thenReturn(output);

        ModuleOutputDevice device = service.create(pk);
        assertNotNull(device);

        MonitoredActionDto action = new MonitoredActionDto();
        action.setId("AAA");

        device.associate(action);
        device.actionBegin();
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.actionEnd();

        Thread.sleep(500);

        verify(storage, times(4)).createModuleOutput(eq(pk), eq(ModuleLoggerMessage.LOG_OUTPUT_TYPE));
        verify(output, times(4)).setId(any());
        verify(output, times(4)).setActionId(eq("AAA"));
        verify(output, times(4)).setPayload(eq("Hello world"));
        verify(storage, times(4)).saveModuleOutput(eq(output));

        verify(actionStorage, times(3)).saveActionState(eq(pk), any(MonitoredActionDto.class));

        assertEquals(MonitoredAction.State.SUCCESS, action.getState());
    }

    @Test
    public void deviceActionOutSuccessOtherAssosiation() throws Exception {
        final String system = "mockSys-2",
                application = "mockApp-2",
                version = "mockVer-2",
                description = "mockDescription-2";

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        pk = new ModuleKeyDto(pk);

        final ModuleLoggerMessage output = mock(ModuleLoggerMessage.class);
        when(storage.createModuleOutput(eq(pk), eq(ModuleLoggerMessage.LOG_OUTPUT_TYPE))).thenReturn(output);

        ModuleOutputDevice device = service.create(pk);
        assertNotNull(device);

        device.associate("Action assosiated by description.");
        device.actionBegin();
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.actionEnd();

        Thread.sleep(500);

        verify(storage, times(4)).createModuleOutput(eq(pk), eq(ModuleLoggerMessage.LOG_OUTPUT_TYPE));
        verify(output, times(4)).setId(any());
        verify(output, times(4)).setActionId(anyString());
        verify(output, times(4)).setPayload(eq("Hello world"));
        verify(storage, times(4)).saveModuleOutput(eq(output));

        verify(actionStorage, times(3)).saveActionState(eq(pk), any(MonitoredAction.class));

        assertEquals(MonitoredAction.State.SUCCESS, device.getAssociated().getState());
    }

    @Test
    public void deviceActionOutFail() throws Exception {
        final String system = "mockSys-3",
                application = "mockApp-3",
                version = "mockVer-3",
                description = "mockDescription-3";

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        pk = new ModuleKeyDto(pk);

        final ModuleLoggerMessage output = mock(ModuleLoggerMessage.class);
        when(storage.createModuleOutput(eq(pk), eq(ModuleLoggerMessage.LOG_OUTPUT_TYPE))).thenReturn(output);

        ModuleOutputDevice device = service.create(pk);
        assertNotNull(device);

        MonitoredActionDto action = new MonitoredActionDto();
        action.setId("AAA");

        device.associate(action);
        device.actionBegin();

        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");

        device.actionFail();


        Thread.sleep(500);

        verify(storage, times(4)).createModuleOutput(eq(pk), eq(ModuleLoggerMessage.LOG_OUTPUT_TYPE));
        verify(output, times(4)).setId(any());
        verify(output, times(4)).setActionId(eq("AAA"));
        verify(output, times(4)).setPayload(eq("Hello world"));
        verify(storage, times(4)).saveModuleOutput(eq(output));

        verify(actionStorage, times(3)).saveActionState(eq(pk), any(MonitoredAction.class));

        assertEquals(MonitoredAction.State.FAIL, action.getState());
    }
    @Test
    public void deviceActionOutIgnored() throws Exception {
        final String system = "mockSys-x",
                application = "mockApp-x",
                version = "mockVer-x",
                description = "mockDescription1";

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        pk = new ModuleKeyDto(pk);

        final ModuleLoggerMessage output = mock(ModuleLoggerMessage.class);
        when(storage.createModuleOutput(eq(pk), eq(ModuleLoggerMessage.LOG_OUTPUT_TYPE))).thenReturn(output);

        ModuleOutputDevice device = service.create(pk);
        assertNotNull(device);

        service.setIgnoreModules(key(pk));

        MonitoredActionDto action = new MonitoredActionDto();
        action.setId("AAA");

        device.associate(action);
        device.actionBegin();
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.actionFail();
        
        Thread.sleep(500);


        verify(output, times(4)).setId(anyString());
        verify(output, times(4)).setActionId(eq("AAA"));
        verify(output, times(4)).setPayload(eq("Hello world"));
        verify(storage, times(4)).saveModuleOutput(eq(output));

        verify(actionStorage, times(3)).saveActionState(eq(pk), any(MonitoredActionDto.class));

        assertEquals(MonitoredAction.State.FAIL, action.getState());
    }


    @Test
    public void getType() throws Exception {
        assertEquals(ModuleLoggerMessage.LOG_OUTPUT_TYPE, service.getType());
    }

    @Test
    public void getModulePK() throws Exception {
        assertEquals(service, service);
    }

    @Test
    public void isActive() throws Exception {
        assertFalse(!service.isActive());
    }

    @Test
    public void restart() throws Exception {
        service.restart();
        assertFalse(!service.isActive());
    }

    @Test
    public void getConfiguration() throws Exception {
        Map<String, ConfiguredVariableItem> cfg = service.getConfiguration();
        assertNotNull(cfg);
        assertEquals(1, cfg.size());
    }

    @Test
    public void configurationChanged() throws Exception {
        final String VarKey = LogModuleServiceImpl.IGNORE_MODULES_FULL_NAME;
        final ConfiguredVariableItemDto item = new ConfiguredVariableItemDto(LogModuleServiceImpl.IGNORE_MODULES_NAME, "test", "VarKey");
        Map<String, ConfiguredVariableItem> changed = new HashMap<>();
        changed.put(VarKey, item);
        assertNotEquals("VarKey", service.getConfiguration().get(VarKey).get(String.class));
        
        service.configurationChanged(changed);
        
        assertEquals("VarKey", service.getConfiguration().get(VarKey).get(String.class));
        assertEquals("VarKey", service.getIgnoreModules());
        
        service.setIgnoreModules("VarKey2");
        
        assertEquals("VarKey2", service.getConfiguration().get(VarKey).get(String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void configurationChangedBad() throws Exception {
        final String VarKey = LogModuleServiceImpl.IGNORE_MODULES_FULL_NAME;
        assertNotEquals("VarKey", service.getConfiguration().get(VarKey).get(Number.class));
        fail("Here the IllegalArgumentException exception must be thown.");
    }

}