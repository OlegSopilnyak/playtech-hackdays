package com.mobenga.health.configuration;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.impl.DistributedContainersServiceHazelcastImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Hazelcast distributed containers
 */
@Configuration
public class DistributedConfiguration {
    @Bean
    public HazelcastInstance hazelcastInstance(){
        return Hazelcast.newHazelcastInstance();

    }

    @Bean
    public DistributedContainersService createService(){
        DistributedContainersServiceHazelcastImpl service = new DistributedContainersServiceHazelcastImpl();
        service.setCacheSystem(hazelcastInstance());
        return service;
    }
}
