package com.mobenga.hm.openbet.service.impl;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.LogMessage;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.transport.ModuleWrapper;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.storage.ConfigurationStorage;
import com.mobenga.health.storage.HeartBeatStorage;
import com.mobenga.health.storage.ModuleOutputStorage;
import com.mobenga.health.storage.MonitoredActionStorage;
import com.mobenga.hm.openbet.configuration.ApplicationConfiguration;
import com.mobenga.hm.openbet.configuration.test.MockedStorageConfiguration;
import com.mobenga.hm.openbet.dto.*;
import com.mobenga.hm.openbet.service.DateTimeConverter;
import com.mobenga.hm.openbet.service.ExternalModuleSupportService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mobenga.health.HealthUtils.key;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for external module support service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {ApplicationConfiguration.class, MockedStorageConfiguration.class},
        loader = AnnotationConfigContextLoader.class)
public class ExternalModuleSupportServiceImplTest {
    @Autowired
    private ExternalModuleSupportService service;
    @Autowired
    private ModuleConfigurationService configService;
    @Autowired
    private LogMessage message;
    @Autowired
    private MonitoredAction action;
    @Autowired
    private HeartBeatStorage hbStorage;
    @Autowired
    private ModuleOutputStorage outputStorage;
    @Autowired
    private MonitoredActionStorage actionStorage;
    @Autowired
    private ConfigurationStorage configStorage;
    @Autowired
    private DateTimeConverter dtConverter;
    @Autowired
    private TimeService timer;

    @Test
    public void pong() throws Exception {
        // adjust mocks
        // module
        final String system = "mockSys",
                application = "mockApp",
                version = "mockVer",
                description = "mockDescription"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        pk = new ModuleWrapper(pk);

        // configuration storage
        when(configStorage.replaceConfiguration(eq(pk), any(Map.class))).then(new Answer<Map>() {
            @Override
            public Map answer(InvocationOnMock invocationOnMock) throws Throwable {
                return (Map) invocationOnMock.getArguments()[1];
            }
        });

        final ConfigurationUpdate update = new ConfigurationUpdate();
        update.setHost("host");
        update.setModule(pk);
        final List<ModuleConfigurationItem> updatedConfig = new ArrayList<>();
        update.setUpdated(updatedConfig);
        updatedConfig.add(configurationItem("1.2.3.4.5.a", "INTEGER", "100"));
        updatedConfig.add(configurationItem("1.2.3.4.5.b", "STRING", "100a"));
        updatedConfig.add(configurationItem("1.2.3.4.5.c", "INTEGER", "300"));
        service.changeConfiguration(update);
        Thread.sleep(300);
        assertEquals(3, configService.getConfigurationGroup(pk,"1.2.3.4").size());

        final ExternalModulePing ping = new ExternalModulePing();
        ping.setHost("host");
        ping.setState("active");
        ping.setModule(pk);
        List<ModuleOutputMessage> output = new ArrayList<>();
        ping.setOutput(output);
        {
            ModuleOutputMessage msg = new ModuleOutputMessage();
            msg.setMessageType("yyy");
            msg.setPayload("Test message 1");
            msg.setWhenOccurred(nowTime());
            output.add(msg);
        }
        {
            ModuleOutputMessage msg = new ModuleOutputMessage();
            msg.setMessageType("yyy");
            msg.setPayload("Test message 2");
            msg.setWhenOccurred(nowTime());
            output.add(msg);
        }
        List<ModuleAction> actions = new ArrayList<>();
        ping.setActions(actions);
        {
            ModuleAction action = new ModuleAction();
            actions.add(action);
            action.setState("SUCCESS");
            action.setDescription("Some activity in external module.");
            action.setDuration(10);
            action.setStartTime(nowTime());
            action.setFinishTime(nowTime());
            List<ModuleOutputMessage> aOutput = new ArrayList<>();
            action.setOutput(aOutput);
            {
                ModuleOutputMessage msg = new ModuleOutputMessage();
                msg.setMessageType("yyy");
                msg.setPayload("Test message 3");
                msg.setWhenOccurred(nowTime());
                aOutput.add(msg);
            }
        }
        List<ModuleConfigurationItem> configuration = new ArrayList<>();
        ping.setConfiguration(configuration);
        {
            ModuleConfigurationItem item = new ModuleConfigurationItem();
            item.setPath("1.2.3.4.5.b");
            item.setType("STRING");
            item.setValue("abcd");
            item.setDescription("For tests only.");
            configuration.add(item);
        }
        {
            ModuleConfigurationItem item = new ModuleConfigurationItem();
            item.setPath("1.2.3.4.5.d");
            item.setType("STRING");
            item.setValue("bcde");
            item.setDescription("For tests only.");
            configuration.add(item);
        }

        reset(hbStorage,outputStorage,actionStorage, configStorage);
        // adjustment of mocks
        when(outputStorage.createModuleOutput(eq(pk), eq(LogMessage.OUTPUT_TYPE))).thenReturn(message);
        when(actionStorage.createMonitoredAction()).thenReturn(action);

        // process ping from external module
        List<ModuleConfigurationItem> changed = service.pong(ping);
        assertNotNull(changed);
        Thread.sleep(300);
        // check the values
        assertEquals(1, changed.size());
        assertEquals("100a", changed.get(0).getValue());
        // check the behavior
        verify(hbStorage,times(1)).saveModuleState(eq(pk), anyBoolean());
        verify(outputStorage,times(3)).createModuleOutput(eq(pk), eq(LogMessage.OUTPUT_TYPE));
        verify(outputStorage,times(3)).saveModuleOutput(eq(message));
        verify(actionStorage,times(1)).createMonitoredAction();
        verify(actionStorage,times(1)).saveActionState(eq(pk), eq(action));
        verify(configStorage, times(1)).storeChangedConfiguration(eq(pk), any(Map.class));
    }

    @Test
    public void changeConfigurationItem() throws Exception {
        final String system = "mockSys-1",
                application = "mockApp-1",
                version = "mockVer-1",
                description = "mockDescription-1"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        when(configStorage.replaceConfiguration(any(HealthItemPK.class), any(Map.class))).then(new Answer<Map>() {
            @Override
            public Map answer(InvocationOnMock invocationOnMock) throws Throwable {
                return (Map) invocationOnMock.getArguments()[1];
            }
        });

        configService.newConfiguredVariables(pk, new HashMap<>());

        String path = "1.2.3.4.5.d", value = "Hello";

        ModuleConfigurationItem changed = service.changeConfigurationItem(key(pk), path, value);
        assertEquals(path,changed.getPath());
        assertEquals(value,changed.getValue());


    }

    @Test
    public void changeConfiguration() throws Exception {
        final String system = "mockSys",
                application = "mockApp",
                version = "mockVer",
                description = "mockDescription"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        configService.newConfiguredVariables(pk, new HashMap<>());
        when(configStorage.replaceConfiguration(any(HealthItemPK.class), any(Map.class))).then(new Answer<Map>() {
            @Override
            public Map answer(InvocationOnMock invocationOnMock) throws Throwable {
                return (Map) invocationOnMock.getArguments()[1];
            }
        });

        ConfigurationUpdate update = new ConfigurationUpdate();
        update.setHost("host");
        update.setModule(pk);
        List<ModuleConfigurationItem> updatedConfig = new ArrayList<>();
        update.setUpdated(updatedConfig);
        updatedConfig.add(configurationItem("1.2.3.4.5.a", "INTEGER", "100"));
        updatedConfig.add(configurationItem("1.2.3.4.5.b", "STRING", "100a"));
        updatedConfig.add(configurationItem("1.2.3.4.5.c", "INTEGER", "300"));

        List<ModuleConfigurationItem> config = service.changeConfiguration(update);
        assertEquals(3, config.size());
        Map<String, ModuleConfigurationItem> cfg = config.stream().collect(Collectors.toMap(i ->i.getPath(), i->i));
        assertEquals("100", cfg.get("1.2.3.4.5.a").getValue());
        assertEquals("100a", cfg.get("1.2.3.4.5.b").getValue());
        assertEquals("300", cfg.get("1.2.3.4.5.c").getValue());
    }

    //    private methods
    private String nowTime(){
        return dtConverter.asString(timer.correctTime());
    }
    private ModuleConfigurationItem configurationItem(String path, String type, String value){
        ModuleConfigurationItem item  = new ModuleConfigurationItem(path, type, value);
        item.setDescription("For tests puposes only.");
        return item;
    }

}