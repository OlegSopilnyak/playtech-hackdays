/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import com.fasterxml.jackson.databind.ObjectMapper;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.UniqueIdGenerator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Configuration: configuration services utility for modules system
 */
@Configuration
class ModuleUtilityConfiguration {
	/**
	 * Service: service to work with time
	 *
	 * @return singleton
	 */
	@Bean
	public TimeService getTimeService() {
		return new TimeService() {
			@Override
			public Instant now() {
				return Instant.now();
			}

			@Override
			public Long duration(Instant start) {
				return Objects.isNull(start) ? -1L : Duration.between(start, now()).toMillis();
			}
		};
	}

	/**
	 * Service: service to generate unique IDs
	 *
	 * @return singleton
	 */
	@Bean
	public UniqueIdGenerator getUniqueIdGenerator(){
		return () -> UUID.randomUUID().toString();
	}

	/**
	 * Service: to transform data to JSON
	 *
	 * @return singleton
	 */
	@Bean
	public ObjectMapper getObjectMapper(){
		return new ObjectMapper();
	}

	/**
	 * Service: thread pool with possibility to schedule execution
	 * @see ScheduledExecutorService
	 *
	 * @return singleton
	 */
	@Bean
	public ScheduledExecutorService getScheduledExecutorService(){
		final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
		executor.setMaximumPoolSize(100);
		return executor;
	}

	@Bean
	@Scope( ConfigurableBeanFactory.SCOPE_PROTOTYPE )
	public Layout<ILoggingEvent> getLayout(){
		PatternLayout layout = new PatternLayout();
		layout.setPattern("[%thread] %-5level %logger{50} - %msg%n");
		return layout;
	}
}
