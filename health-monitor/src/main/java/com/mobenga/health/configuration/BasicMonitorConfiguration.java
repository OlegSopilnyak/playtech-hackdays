package com.mobenga.health.configuration;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Spring configuration for monitor basics (without persistence)
 */
@Configuration
@ImportResource({
        "classpath:/META-INF/spring/com/mobenga/health/monitor-basic.xml"
})
@PropertySource("classpath:/META-INF/spring/com/mobenga/health/module.properties")
public class BasicMonitorConfiguration {

    @Value("${threads.pool.init:7}")
    private int corePoolSize;

    @Bean
    public HazelcastInstance hazelcastInstance(){
        return Hazelcast.newHazelcastInstance();

    }

    @Bean(name = "serviceRunner")
    public ExecutorService serviceRunner(){
        return Executors.newScheduledThreadPool(corePoolSize);
    }
    
}
