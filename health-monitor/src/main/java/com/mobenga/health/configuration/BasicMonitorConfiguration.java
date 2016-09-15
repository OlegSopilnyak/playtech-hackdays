package com.mobenga.health.configuration;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.health.model.factory.impl.TimeServiceImpl;
import com.mobenga.health.model.factory.impl.UniqueIdGeneratorImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import java.util.Map;
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
