package com.mobenga.hm.openbet.configuration;

import com.mobenga.health.configuration.OpenbetPersistenceConfiguration;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.health.storage.OpenBetOperationStorage;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
import com.mobenga.hm.openbet.service.impl.OpenbetOperationsManipulationServiceImpl;
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
@Import({OpenbetPersistenceConfiguration.class})
@ComponentScan(basePackages = "com.mobenga.hm.openbet.controller")
public class ApplicationConfiguration {
    
    @Autowired
    private UniqueIdGenerator idGenerator;

    @Autowired
    private OpenBetOperationStorage operationStorage;

    @Bean(autowire = Autowire.BY_TYPE, initMethod = "initStorage")
    public OpenbetOperationsManipulationService createOperationsStorage(){
        return new OpenbetOperationsManipulationServiceImpl();
    }
}
