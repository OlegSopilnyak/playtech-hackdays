package com.mobenga.health.configuration;

import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.health.model.factory.impl.TimeServiceImpl;
import com.mobenga.health.model.factory.impl.UniqueIdGeneratorImpl;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for factory package services
 */
@Configuration
public class FactoryConfiguration {

    /**
     * Time Service realization
     *
     * @return instance
     */
    @Bean(name = {"timeService"}, autowire = Autowire.BY_TYPE)
    public TimeService makeTimeService() {
        TimeServiceImpl instance = new TimeServiceImpl();
        return instance;
    }

    /**
     * Service to generate Unique ID
     *
     * @return instance
     */
    @Bean(name = {"uuidGeneratorService"}, autowire = Autowire.BY_TYPE)
    public UniqueIdGenerator makeUniqueIdGenerator() {
        UniqueIdGeneratorImpl instance = new UniqueIdGeneratorImpl();
        return instance;
    }
}
