package com.mobenga.health.configuration;

import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.health.storage.impl.SimpleFileStorageImpl;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration class for monitor persistence
 */
@Configuration
@Import(FactoryConfiguration.class)
public class PersistenceConfiguration {

    @Autowired
    private TimeService timeService;
    @Autowired
    private UniqueIdGenerator idGenerator;

    @Bean(name = "coreStorage", autowire = Autowire.BY_TYPE)
    public SimpleFileStorageImpl monitorCoreStorage() {
        return new SimpleFileStorageImpl();
    }

}
