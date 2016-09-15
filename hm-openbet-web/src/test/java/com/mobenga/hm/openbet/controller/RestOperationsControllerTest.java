package com.mobenga.hm.openbet.controller;

import com.mobenga.hm.openbet.configuration.test.RestControllerTestConfiguration;
import com.mobenga.hm.openbet.dto.MonitorCriteria;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

/**
 * Integration test for REST calls
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = RestControllerTestConfiguration.class, loader = AnnotationConfigWebContextLoader.class)
public class RestOperationsControllerTest {

    @Autowired
    private RestOperationsController controller;

    @Autowired
    private OpenbetOperationsManipulationService service;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testSearchOperations() throws Exception {
        MvcResult result =
                this.mockMvc.perform(
                        post("/monitor/openbet/operations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )

                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andReturn();
        String value = result.getResponse().getContentAsString();
        assertTrue(value.startsWith("[") && value.endsWith("]"));
        verify(service, times(1)).selectOperationsByCriteria(any());
    }

    @Test
    public void testCountOpeartions() throws Exception {
        MvcResult result =
                this.mockMvc.perform(
                        post("/monitor/openbet/operations/count")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )

                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andReturn();
        String value = result.getResponse().getContentAsString();
        assertEquals("0", value);
        verify(service, times(1)).countOperationByCriteria(any());
    }

    @Test
    public void testOperationTypes() throws Exception {
        MvcResult result =
                this.mockMvc.perform(get("/monitor/openbet/types").accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andReturn();
        String value = result.getResponse().getContentAsString();
        assertTrue(value.startsWith("[") && value.endsWith("]"));
        verify(service, times(1)).supportedOperationTypes();
    }
}