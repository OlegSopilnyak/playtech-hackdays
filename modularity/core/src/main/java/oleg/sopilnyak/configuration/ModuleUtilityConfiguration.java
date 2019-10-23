/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
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
public class ModuleUtilityConfiguration {

	public static final int MAXIMUM_POOL_SIZE = 100;

	/**
	 * Service: service to work with time
	 *
	 * @return singleton
	 */
	@Bean
	public TimeService getTimeService() {
		return new TimeServiceImpl();
	}

	/**
	 * Service: service to generate unique IDs
	 *
	 * @return singleton
	 */
	@Bean
	public UniqueIdGenerator getUniqueIdGenerator() {
		return new UniqueIdGeneratorImpl();
	}

	/**
	 * Service: to transform data to JSON and back
	 *
	 * @return singleton
	 */
	@Bean
	public ObjectMapper getObjectMapper() {
		final String propertyName = "$type";
		final ObjectMapper.DefaultTyping applicability = ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE;

		final ObjectMapper mapper = new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//				.activateDefaultTypingAsProperty(NoCheckSubTypeValidator.instance, applicability, propertyName)
				.enableDefaultTypingAsProperty(applicability, propertyName);
		final VisibilityChecker checker = mapper.getSerializationConfig()
				.getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
				.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE);
		mapper.setVisibility(checker);
		return mapper;
	}

	/**
	 * Service: thread pool with possibility to schedule execution
	 *
	 * @return singleton
	 * @see ScheduledExecutorService
	 */
	@Bean
	public ScheduledExecutorService getScheduledExecutorService() {
		final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
		executor.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
		return executor;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public Layout<ILoggingEvent> getLayout() {
		PatternLayout layout = new PatternLayout();
		layout.setPattern("[%thread] %-5level %logger{50} - %msg%n");
		return layout;
	}

	// inner classes
	private static class TimeServiceImpl implements TimeService {
		/**
		 * To get current date-time
		 *
		 * @return current
		 */
		@Override
		public Instant now() {
			return Instant.now();
		}

		/**
		 * To calculate duration between started and now in milliseconds
		 *
		 * @param start time of begin
		 * @return duration value
		 */
		@Override
		public Long duration(Instant start) {
			return Objects.isNull(start) ? -1L : Duration.between(start, now()).toMillis();
		}
	}

	private static class UniqueIdGeneratorImpl implements UniqueIdGenerator {

		/**
		 * To generate unique id
		 *
		 * @return unique id
		 */
		@Override
		public String generate() {
			return UUID.randomUUID().toString();
		}
	}
}
