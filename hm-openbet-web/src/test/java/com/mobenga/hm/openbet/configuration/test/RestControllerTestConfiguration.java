package com.mobenga.hm.openbet.configuration.test;

import com.mobenga.health.configuration.OpenbetPersistenceConfiguration;
import com.mobenga.hm.openbet.controller.ApplicationController;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
import com.mobenga.hm.openbet.service.stub.OpenbetOperationsManipulationServiceStub;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;

/**
 * Test configuration for the rest
 */
@Configuration
@ComponentScan(basePackages = "com.mobenga.hm.openbet.controller",
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ApplicationController.class)}
)
@EnableWebMvc
public class RestControllerTestConfiguration {

    @Bean
    public OpenbetOperationsManipulationService createOperationsStorage(){
        OpenbetOperationsManipulationService service = mock(OpenbetOperationsManipulationService.class);
        return service;
    }
}
