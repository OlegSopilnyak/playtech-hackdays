package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.*;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.model.transport.ModuleWrapper;
import com.mobenga.health.storage.ModuleOutputStorage;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static com.mobenga.health.HealthUtils.key;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static sun.audio.AudioDevice.device;

/**
 * Tests for log module-output service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:com/mobenga/health/monitor/impl/test-module-log-service.xml",
        "classpath:com/mobenga/health/monitor/factory/impl/test-basic-monitor-services.xml"

})
public class LogModuleServiceImplTest {

    @Autowired
    private LogModuleServiceImpl service;
    @Autowired
    private ModuleOutputStorage storage;
    @Autowired
    private MonitoredActionStorage actionStorage;

    @Test
    public void startService() throws Exception {
        if (service.isActive()) service.stopService();
        service.startService();
        assertFalse(!service.isActive());
    }

    @Test
    public void stopService() throws Exception {
        if (service.isActive()) {
            service.stopService();
        } else {
            service.startService();
            service.stopService();
        }
        assertFalse(service.isActive());
        service.startService();
    }

    @Test
    public void create() throws Exception {
        final String system = "mockSys",
                application = "mockApp",
                version = "mockVer",
                description = "mockDescription1";

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);


        ModuleOutput.Device device = service.create(pk);

        assertNotNull(device);

        LogMessage output = mock(LogMessage.class);
        when(storage.createModuleOutput(any(HealthItemPK.class), eq(LogMessage.OUTPUT_TYPE))).thenReturn(output);

        device.out("Hello world");
        Thread.sleep(100);
    }

    @Test
    public void deviceOut() throws Exception {
        final String system = "mockSys-1",
                application = "mockApp-1",
                version = "mockVer-1",
                description = "mockDescription1";

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        pk = new ModuleWrapper(pk);

        ModuleOutput.Device device = service.create(pk);
        assertNotNull(device);

        LogMessage output = mock(LogMessage.class);
        when(storage.createModuleOutput(eq(pk), eq(LogMessage.OUTPUT_TYPE))).thenReturn(output);

        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        Thread.sleep(1000);

        verify(storage, times(4)).createModuleOutput(eq(pk), eq(LogMessage.OUTPUT_TYPE));
        verify(output, times(4)).setId(any());
        verify(output, times(4)).setActionId(anyString());
        verify(output, times(4)).setPayload(eq("Hello world"));
        verify(storage, times(4)).saveModuleOutput(eq(output));
        reset(storage);
    }

    @Test
    public void deviceActionOutSuccess() throws Exception {
        final String system = "mockSys-2",
                application = "mockApp-2",
                version = "mockVer-2",
                description = "mockDescription-2";

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        pk = new ModuleWrapper(pk);

        reset(storage, actionStorage);
        ModuleOutput.Device device = service.create(pk);
        assertNotNull(device);

        LogMessage output = mock(LogMessage.class);
        when(storage.createModuleOutput(eq(pk), eq(LogMessage.OUTPUT_TYPE))).thenReturn(output);

        MonitoredActionStub action = new MonitoredActionStub();
        action.setId("AAA");

        device.associate(action);
        device.actionBegin();
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.actionEnd();

        Thread.sleep(1000);

        verify(storage, times(4)).createModuleOutput(eq(pk), eq(LogMessage.OUTPUT_TYPE));
        verify(output, times(4)).setId(any());
        verify(output, times(4)).setActionId(eq("AAA"));
        verify(output, times(4)).setPayload(eq("Hello world"));
        verify(storage, times(4)).saveModuleOutput(eq(output));

        verify(actionStorage, times(3)).saveActionState(eq(pk), any(MonitoredActionStub.class));

        assertEquals(MonitoredAction.State.SUCCESS, action.getState());
        reset(storage, actionStorage);
    }

    @Test
    public void deviceActionOutFail() throws Exception {
        final String system = "mockSys-3",
                application = "mockApp-3",
                version = "mockVer-3",
                description = "mockDescription-3";

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        reset(storage, actionStorage);
        ModuleOutput.Device device = service.create(pk);
        assertNotNull(device);

        LogMessage output = mock(LogMessage.class);
        when(storage.createModuleOutput(any(HealthItemPK.class), eq(LogMessage.OUTPUT_TYPE))).thenReturn(output);

        MonitoredActionStub action = new MonitoredActionStub();
        action.setId("AAA");

        device.associate(action);
        device.actionBegin();
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.actionFail();


        Thread.sleep(1000);

        verify(storage, times(4)).createModuleOutput(any(HealthItemPK.class), eq(LogMessage.OUTPUT_TYPE));
        verify(output, times(4)).setId(any());
        verify(output, times(4)).setActionId(eq("AAA"));
        verify(output, times(4)).setPayload(eq("Hello world"));
        verify(storage, times(4)).saveModuleOutput(eq(output));

        verify(actionStorage, times(3)).saveActionState(any(HealthItemPK.class), any(MonitoredActionStub.class));

        assertEquals(MonitoredAction.State.FAIL, action.getState());
        reset(storage, actionStorage);
    }
    @Test
    public void deviceActionOutIgnored() throws Exception {
        final String system = "mockSys-x",
                application = "mockApp-x",
                version = "mockVer-x",
                description = "mockDescription1";

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        pk = new ModuleWrapper(pk);

        reset(storage, actionStorage);
        ModuleOutput.Device device = service.create(pk);
        assertNotNull(device);

        service.setIgnoreModules(key(pk));

        LogMessage output = mock(LogMessage.class);
        when(storage.createModuleOutput(eq(pk), eq(LogMessage.OUTPUT_TYPE))).thenReturn(output);

        MonitoredActionStub action = new MonitoredActionStub();
        action.setId("AAA");

        device.associate(action);
        device.actionBegin();
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.out("Hello world");
        device.actionFail();
        Thread.sleep(1000);


        verify(storage, times(0)).createModuleOutput(eq(pk), eq(LogMessage.OUTPUT_TYPE));
        verify(output, times(0)).setId(any());
        verify(output, times(0)).setActionId(eq("AAA"));
        verify(output, times(0)).setPayload(eq("Hello world"));
        verify(storage, times(0)).saveModuleOutput(eq(output));

        verify(actionStorage, times(3)).saveActionState(eq(pk), any(MonitoredActionStub.class));

        assertEquals(MonitoredAction.State.FAIL, action.getState());
        reset(storage, actionStorage);
    }


    @Test
    public void getType() throws Exception {
        assertEquals(LogMessage.OUTPUT_TYPE, service.getType());
    }

    @Test
    public void getModulePK() throws Exception {
        assertEquals(service.getModulePK(), service);
    }

    @Test
    public void isActive() throws Exception {
        if (service.isActive()) service.stopService();
        service.startService();
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
        final String VarKey = LogModuleServiceImpl.PARAMS_PACKAGE+"."+LogModuleServiceImpl.IGNORE_MODULES;
        final LocalConfiguredVariableItem item = new LocalConfiguredVariableItem(LogModuleServiceImpl.IGNORE_MODULES, "test", "VarKey");
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
        final String VarKey = LogModuleServiceImpl.PARAMS_PACKAGE+"."+LogModuleServiceImpl.IGNORE_MODULES;
        assertNotEquals("VarKey", service.getConfiguration().get(VarKey).get(Number.class));
        fail("Here the IllegalArgumentException exception must be thown.");
    }

}