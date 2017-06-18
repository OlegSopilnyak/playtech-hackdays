package com.mobenga.health.configuration;

import com.mobenga.health.monitor.HealthModuleService;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.monitor.impl.HealthModuleServiceImpl;
import com.mobenga.health.monitor.impl.ModuleConfigurationServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowire;

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

    @Bean(name = "serviceRunner")
    public ExecutorService serviceRunner(){
        return Executors.newScheduledThreadPool(corePoolSize);
    }
    
    @Bean(name = "moduleConfigurationService",
            autowire = Autowire.BY_TYPE,
            initMethod = "initialize",
            destroyMethod = "shutdown")
    public ModuleConfigurationService configurationServive(){
        return new ModuleConfigurationServiceImpl();
    }
    
    @Bean(name = "moduleHealthService",
            autowire = Autowire.BY_TYPE,
            initMethod = "initialize",
            destroyMethod = "shutdown")
//    @DependsOn({""})
    public HealthModuleService healthService(){
        return new HealthModuleServiceImpl();
    }
}
