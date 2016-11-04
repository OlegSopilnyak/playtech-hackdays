package com.mobenga.hm.openbet.configuration.test;

import com.mobenga.health.configuration.BasicMonitorConfiguration;
import com.mobenga.health.configuration.FactoryConfiguration;
import com.mobenga.health.configuration.PersistenceConfiguration;
import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.LogMessage;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.storage.*;
import com.mobenga.hm.openbet.configuration.ApplicationConfiguration;
import com.mobenga.hm.openbet.controller.ApplicationController;
import com.mobenga.hm.openbet.service.ExternalModuleSupportService;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration for the rest
 */
@Configuration
@ComponentScan(basePackages = "com.mobenga.hm.openbet.controller",
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ApplicationController.class)}
)
@Import({ApplicationConfiguration.class, FactoryConfiguration.class, MockedStorageConfiguration.class})
@EnableWebMvc
public class RestControllerTestConfiguration {

    @Bean
    public ExternalModuleSupportService moduleSupport(){
        return mock(ExternalModuleSupportService.class);
    }

}
