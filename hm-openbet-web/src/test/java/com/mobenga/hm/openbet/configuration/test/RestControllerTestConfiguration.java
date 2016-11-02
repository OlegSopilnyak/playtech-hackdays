package com.mobenga.hm.openbet.configuration.test;

import com.mobenga.health.configuration.BasicMonitorConfiguration;
import com.mobenga.health.configuration.FactoryConfiguration;
import com.mobenga.health.configuration.PersistenceConfiguration;
import com.mobenga.hm.openbet.configuration.ApplicationConfiguration;
import com.mobenga.hm.openbet.controller.ApplicationController;
import com.mobenga.hm.openbet.service.ExternalModuleSupportService;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;

/**
 * Test configuration for the rest
 */
@Configuration
@ComponentScan(basePackages = "com.mobenga.hm.openbet.controller",
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ApplicationController.class)}
)
@Import({ApplicationConfiguration.class})
@EnableWebMvc
public class RestControllerTestConfiguration {

    @Bean
    public ExternalModuleSupportService moduleSupport(){
        return mock(ExternalModuleSupportService.class);
    }


    @Bean
    public OpenbetOperationsManipulationService createOperationsStorage(){
        OpenbetOperationsManipulationService service = mock(OpenbetOperationsManipulationService.class);
        return service;
    }
}
