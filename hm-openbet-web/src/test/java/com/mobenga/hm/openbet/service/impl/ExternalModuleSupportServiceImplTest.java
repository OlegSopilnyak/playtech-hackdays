package com.mobenga.hm.openbet.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mobenga.health.configuration.PersistenceConfiguration;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.storage.ConfigurationStorage;
import com.mobenga.hm.openbet.configuration.ApplicationConfiguration;
import com.mobenga.hm.openbet.configuration.test.MockedStorageConfiguration;
import com.mobenga.hm.openbet.dto.ExternalModulePing;
import com.mobenga.hm.openbet.dto.ModuleAction;
import com.mobenga.hm.openbet.dto.ModuleConfigurationItem;
import com.mobenga.hm.openbet.dto.ModuleOutputMessage;
import com.mobenga.hm.openbet.service.DateTimeConverter;
import com.mobenga.hm.openbet.service.ExternalModuleSupportService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mobenga.health.HealthUtils.key;
import static com.sun.corba.se.spi.activation.IIOP_CLEAR_TEXT.value;
import static org.junit.Assert.*;
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

        String path = "1.2.3.4.5.a", value = "Hello";

        ModuleConfigurationItem changed = service.changeConfigurationItem(key(pk), path, value);
        assertEquals(path,changed.getPath());
        assertEquals(value,changed.getValue());
    }

    @Test
    public void changeConfiguration() throws Exception {

    }

    //    private methods
    private String nowTime(){
        return dtConverter.asString(timer.correctTime());
    }

}