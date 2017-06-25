package com.mobenga.health.configuration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.impl.DistributedContainersServiceHazelcastImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Hazelcast distributed containers
 */
@Configuration
public class DistributedConfiguration {
    
    @Value("${configuration.shared.cluster.name:'modules-health-and-configuration'}")
    private String clusterName;
    @Bean
    public HazelcastInstance hazelcastInstance(){
        Config distributedConfig = new Config();
        distributedConfig.getGroupConfig().setName(clusterName).setPassword(clusterName);
        return Hazelcast.newHazelcastInstance(distributedConfig);

    }

    @Bean
    public DistributedContainersService createService(){
        DistributedContainersServiceHazelcastImpl service = new DistributedContainersServiceHazelcastImpl();
        service.setCacheSystem(hazelcastInstance());
        return service;
    }
}
