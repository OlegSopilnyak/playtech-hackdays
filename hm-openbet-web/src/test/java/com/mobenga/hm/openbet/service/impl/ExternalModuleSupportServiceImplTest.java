package com.mobenga.hm.openbet.service.impl;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.storage.ConfigurationStorage;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    private ConfigurationStorage configStorage;
    @Autowired
    private DateTimeConverter dtConverter;
    @Autowired
    private TimeService timer;

    @Test
    public void pong() throws Exception {
        ExternalModulePing ping = new ExternalModulePing();
        ping.setHost("host");
        ping.setState("active");
        ping.setModulePK("system|application|version");
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
            action.setState("SUCCESS");
            action.setDescription("Some activity in external module.");
            action.setDuration(10);
            action.setName("ImportantAction");
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
            item.setPath("1.2.3.a");
            item.setType("STRING");
            item.setValue("abcd");
            configuration.add(item);
        }
        {
            ModuleConfigurationItem item = new ModuleConfigurationItem();
            item.setPath("1.2.3.b");
            item.setType("STRING");
            item.setValue("bcde");
            configuration.add(item);
        }
        List<ModuleConfigurationItem> changed = service.pong(ping);
        assertNotNull(changed);
    }

    @Test
    public void changeConfigurationItem() throws Exception {
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
        update.setModulePK(key(pk));
        List<ModuleConfigurationItem> updatedConfig = new ArrayList<>();
        update.setUpdated(updatedConfig);
        updatedConfig.add(new ModuleConfigurationItem("1.2.3.4.5.a", "INTEGER", "100"));
        updatedConfig.add(new ModuleConfigurationItem("1.2.3.4.5.b", "STRING", "100a"));
        updatedConfig.add(new ModuleConfigurationItem("1.2.3.4.5.c", "INTEGER", "300"));

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

}