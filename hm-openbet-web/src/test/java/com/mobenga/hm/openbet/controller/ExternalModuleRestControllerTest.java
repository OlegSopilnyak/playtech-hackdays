package com.mobenga.hm.openbet.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobenga.health.HealthUtils;
import com.mobenga.health.model.ModuleOutput;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.hm.openbet.configuration.test.RestControllerTestConfiguration;
import com.mobenga.hm.openbet.dto.ExternalModulePing;
import com.mobenga.hm.openbet.dto.ModuleAction;
import com.mobenga.hm.openbet.dto.ModuleConfigurationItem;
import com.mobenga.hm.openbet.dto.ModuleOutputMessage;
import com.mobenga.hm.openbet.service.DateTimeConverter;
import com.mobenga.hm.openbet.service.ExternalModuleSupportService;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test of external module support rest service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = RestControllerTestConfiguration.class, loader = AnnotationConfigWebContextLoader.class)
public class ExternalModuleRestControllerTest {
    private MediaType contentType =
            new MediaType(
                    MediaType.APPLICATION_JSON.getType(),
                    MediaType.APPLICATION_JSON.getSubtype(),
                    Charset.forName("utf8")
            );

    @Autowired
    private ExternalModuleSupportService moduleSupport;
    @Autowired
    private DateTimeConverter dtConverter;
    @Autowired
    private TimeService timer;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private RequestMappingHandlerAdapter adapter;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mappingJackson2HttpMessageConverter = adapter.getMessageConverters()
                .stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny().get();

        assertNotNull("the JSON message converter must be not null", this.mappingJackson2HttpMessageConverter);
    }
    @Test
    public void exchange() throws Exception {
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
        MvcResult result =
                this.mockMvc.perform(
                        post("/module/ping")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(ping))
                )

                        .andExpect(status().isOk())
                        .andExpect(content().contentType(contentType))
                        .andReturn();

        List<ModuleConfigurationItem> config =
                fromJson(result.getResponse().getContentAsString(), new TypeReference<List<ModuleConfigurationItem>>() {});
        assertNotNull(config);
        assertEquals(0, config.size());
        verify(moduleSupport, times(1)).pong(any());
        reset(moduleSupport);

        when(moduleSupport.pong(any())).thenReturn(configuration);
        result = this.mockMvc.perform(
                        post("/module/ping")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(ping))
                )

                        .andExpect(status().isOk())
                        .andExpect(content().contentType(contentType))
                        .andReturn();

        verify(moduleSupport, times(1)).pong(any());
        config =  fromJson(result.getResponse().getContentAsString(), new TypeReference<List<ModuleConfigurationItem>>() {});
        assertNotNull(config);
        assertEquals(2, config.size());
        assertEquals("abcd", config.get(0).getValue());
        assertEquals("bcde", config.get(1).getValue());
    }

    @Test
    public void change() throws Exception {

    }
    //    private methods
    private String toJson(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
    private <T> T fromJson(String json, TypeReference clazz) throws Exception{
        return mapper.readValue(json, clazz);
    }
    private String nowTime(){
        return dtConverter.asString(timer.correctTime());
    }

}