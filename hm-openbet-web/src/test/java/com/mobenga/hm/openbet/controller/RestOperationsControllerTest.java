package com.mobenga.hm.openbet.controller;

import com.mobenga.health.HealthUtils;
import com.mobenga.hm.openbet.configuration.test.RestControllerTestConfiguration;
import com.mobenga.hm.openbet.dto.MonitorCriteria;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for REST calls
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = RestControllerTestConfiguration.class, loader = AnnotationConfigWebContextLoader.class)
public class RestOperationsControllerTest {

    private MediaType contentType =
            new MediaType(
                    MediaType.APPLICATION_JSON.getType(),
                    MediaType.APPLICATION_JSON.getSubtype(),
                    Charset.forName("utf8")
            );

    @Autowired
    private OpenbetOperationsManipulationService service;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private RequestMappingHandlerAdapter adapter;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

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
    public void testSearchOperations() throws Exception {
        final MonitorCriteria criteria = new MonitorCriteria();
        criteria.setBet("The bet...");
        criteria.setCustomer("x-customer");
        criteria.setFromDate(HealthUtils.fromDate(new Date()));
        criteria.setOperationType("read");
        criteria.setToDate(HealthUtils.fromDate(new Date()));
        MvcResult result =
                this.mockMvc.perform(
                        post("/monitor/openbet/operations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(toJson(criteria))
                )

                        .andExpect(status().isOk())
                        .andExpect(content().contentType(contentType))
                        .andReturn();

        List<String> operations =  fromJson(result.getResponse().getContentAsString(), List.class);
        assertNotNull(operations);
        verify(service, times(1)).selectOperationsByCriteria(any());
    }

    @Test
    public void testCountOpeartions() throws Exception {
        final MonitorCriteria criteria = new MonitorCriteria();
        criteria.setBet("The bet...");
        criteria.setCustomer("x-customer");
        criteria.setFromDate(HealthUtils.fromDate(new Date()));
        criteria.setOperationType("read");
        criteria.setToDate(HealthUtils.fromDate(new Date()));
        MvcResult result =
                this.mockMvc.perform(
                        post("/monitor/openbet/operations/count")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(criteria))
                )

                        .andExpect(status().isOk())
                        .andExpect(content().contentType(contentType))
                        .andReturn();

        assertEquals("0", result.getResponse().getContentAsString());
        verify(service, times(1)).countOperationByCriteria(any());
    }

    @Test
    public void testOperationTypes() throws Exception {
        MvcResult result =
                this.mockMvc.perform(
                        get("/monitor/openbet/types")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk()
                )
                        .andExpect(content().contentType(contentType))
                        .andReturn();

        List<String> types =  fromJson(result.getResponse().getContentAsString(), List.class);
        assertNotNull(types);
        verify(service, times(1)).supportedOperationTypes();
    }
//    private methods
    private String toJson(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
    private <T> T fromJson(String json, Class<T> clazz) throws IOException{
        return clazz.cast(this.mappingJackson2HttpMessageConverter.read(clazz, new MockHttpInputMessage(json.getBytes())));
    }
}