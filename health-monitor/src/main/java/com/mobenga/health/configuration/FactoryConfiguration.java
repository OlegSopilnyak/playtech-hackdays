package com.mobenga.health.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Configuration class for factory package services
 */
@Configuration
@ImportResource("classpath:/META-INF/spring/com/mobenga/health/monitor-factory-services.xml")
public class FactoryConfiguration {
}
