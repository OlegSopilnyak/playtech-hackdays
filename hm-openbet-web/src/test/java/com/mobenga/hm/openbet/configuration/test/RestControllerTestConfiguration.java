package com.mobenga.hm.openbet.configuration.test;

import com.mobenga.health.configuration.FactoryConfiguration;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.impl.DistributedContainersServiceTrivialImpl;
import com.mobenga.hm.openbet.configuration.ApplicationConfiguration;
import com.mobenga.hm.openbet.controller.ApplicationController;
import com.mobenga.hm.openbet.service.ExternalModuleSupportService;
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
@Import({ApplicationConfiguration.class, FactoryConfiguration.class, MockedStorageConfiguration.class})
@EnableWebMvc
public class RestControllerTestConfiguration {

    @Bean
    public ExternalModuleSupportService moduleSupport(){
        return mock(ExternalModuleSupportService.class);
    }
    @Bean
    public DistributedContainersService makeDistributed(){
        return new DistributedContainersServiceTrivialImpl();
    }

}
