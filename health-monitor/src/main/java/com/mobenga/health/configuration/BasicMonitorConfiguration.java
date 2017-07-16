package com.mobenga.health.configuration;

import com.mobenga.health.monitor.impl.*;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The Spring configuration for monitor basics (without persistence)
 */
@Configuration
@PropertySource(
        value = {"classpath:/META-INF/spring/com/mobenga/health/module.properties"}, 
        ignoreResourceNotFound = false)
public class BasicMonitorConfiguration {

    @Value("${config.threads.pool.init:2}")
    private int corePoolSize;
    @Value("${heart.beat.delay:2000}")
    private int heartbeatDelay;
    /**
     * Property placeholder configurer needed to process @Value annotations
     * @return instance
     */
     @Bean
     public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
     }
    /**
     * Runner for run core background threads
     * 
     * @return instance
     */
    @Bean(name = "serviceRunner", destroyMethod = "shutdown")
    public ScheduledExecutorService serviceRunner(){
        final ScheduledExecutorService runner = Executors.newScheduledThreadPool(corePoolSize);
        return runner;
    }
    /**
     * The facility log activity inside module
     * 
     * @return instance
     */
    @Bean(name = "modulesOuputService", autowire = Autowire.BY_TYPE, 
            initMethod = "startService", destroyMethod = "stopService")
    public LogModuleServiceImpl makeLogDeviece(){
        LogModuleServiceImpl logOutputDevice = new LogModuleServiceImpl();
        logOutputDevice.setIgnoreModules("healthMonitor|serviceStateScanner|0.1");
        return logOutputDevice;
    }
    
    /**
     * Service to manage configurations of modules
     * 
     * @return instance
     */
    @Bean(name = "modulesConfigurationService", autowire = Autowire.BY_TYPE,
            initMethod = "initialize", destroyMethod = "shutdown")
    @DependsOn("healthModulesManagement")
    public ModuleConfigurationServiceImpl configurationService(){
        ModuleConfigurationServiceImpl service = new ModuleConfigurationServiceImpl();
        return service;
    }
    
    /**
     * Service to manage modules
     * 
     * @return instance
     */
    @Bean(name = "healthModulesManagement", autowire = Autowire.BY_TYPE,
            initMethod = "initialize",  destroyMethod = "shutdown")
    public HealthModuleServiceImpl modulesService(){
        return new HealthModuleServiceImpl();
    }
    
    /**
     * Service scan the condition of registered modules and apply configuration changes to them
     * 
     * @return instance
     */
    @Bean(name = "serviceStateScanner", autowire = Autowire.BY_TYPE, 
            initMethod = "startService", destroyMethod = "stopService")
    @DependsOn("modulesOuputService")
    public ModuleStateNotificationServiceImpl makeStateScanner(){
        ModuleStateNotificationServiceImpl scanner = new ModuleStateNotificationServiceImpl();
        scanner.setHeartbeatDelay(heartbeatDelay);
        return scanner;
    }
    /**
     * Service manage life-cycle of MonitoredActions of modules
     * 
     * @return instance
     */
    @Bean(name = "monitoredActionsService", autowire = Autowire.BY_TYPE,
            initMethod = "initialize", destroyMethod = "shutdown")
    public ModuleActionMonitorServiceImpl makeMonitoredActions(){
        ModuleActionMonitorServiceImpl service = new ModuleActionMonitorServiceImpl();
        service.setIgnoreModules("healthMonitor|serviceStateScanner|0.1");
        return service;
    }
}
