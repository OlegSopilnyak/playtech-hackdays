package com.mobenga.hm.openbet.configuration;

import com.mobenga.health.configuration.BasicMonitorConfiguration;
import com.mobenga.health.configuration.FactoryConfiguration;
import com.mobenga.health.configuration.PersistenceConfiguration;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.hm.openbet.service.DateTimeConverter;
import com.mobenga.hm.openbet.service.ExternalModuleSupportService;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
import com.mobenga.hm.openbet.service.impl.DateTimeConverterImpl;
import com.mobenga.hm.openbet.service.impl.ExternalModuleSupportServiceImpl;
import com.mobenga.hm.openbet.service.stub.OpenbetOperationsManipulationServiceStub;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration of business-logic
 */
@Configuration
//@Import({OpenbetPersistenceConfiguration.class})
@Import({BasicMonitorConfiguration.class, FactoryConfiguration.class, PersistenceConfiguration.class})
@ComponentScan(basePackages = "com.mobenga.hm.openbet.controller")
public class ApplicationConfiguration {
    
    @Autowired
    private UniqueIdGenerator idGenerator;

//    @Autowired
//    private OpenBetOperationStorage operationStorage;

    @Bean(autowire = Autowire.BY_TYPE, initMethod = "initStorage")
    public OpenbetOperationsManipulationService createOperationsStorage(){
//        return new OpenbetOperationsManipulationServiceImpl();
        return new OpenbetOperationsManipulationServiceStub();
    }

    @Bean(autowire = Autowire.BY_TYPE, initMethod = "initialize")
    public ExternalModuleSupportService externalModuleSupportService(){
        return new ExternalModuleSupportServiceImpl();
    }

    @Bean
    public DateTimeConverter createDateTimeConverter(){
        return new DateTimeConverterImpl();
    }
}
