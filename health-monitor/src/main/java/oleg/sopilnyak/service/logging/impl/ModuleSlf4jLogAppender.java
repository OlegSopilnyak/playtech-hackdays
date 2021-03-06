/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.logging.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.ModuleServiceAdapter;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.dto.VariableItemDto;
import oleg.sopilnyak.service.logging.ModuleLoggerService;
import oleg.sopilnyak.service.metric.impl.ModuleMetricAdapter;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service: appender for slf4j logging framework
 */
@Slf4j
public class ModuleSlf4jLogAppender extends AppenderBase<ILoggingEvent> implements ModuleLoggerService {

	@Autowired
	private ModuleActionFactory actionFactory;
	@Autowired
	private Layout<ILoggingEvent> eventLayout;
	@Autowired
	private ModulesRegistry modulesRegistry;
	@Autowired
	private TimeService timeService;

	@Autowired
	private AutowireCapableBeanFactory autowireCapableBeanFactory;

	// configurable attributes
	private int levelSeverity = LEVEL_DEFAULT;
	private String layoutPattern = PATTERN_DEFAULT;

	// instance to delegate calls module-service calls
	private final ModuleServiceAdapter delegate = new ServiceDelegate();
	private final AtomicBoolean delegateInstalled = new AtomicBoolean(false);

	public ModuleSlf4jLogAppender() {
		getConfiguration().put(levelName(), new VariableItemDto(LEVEL_NAME, LEVEL_DEFAULT));
		getConfiguration().put(patternName(), new VariableItemDto(PATTERN_NAME, PATTERN_DEFAULT));
	}

	/**
	 * To start module activity
	 */
	@Override
	public void moduleStart() {
		if (!delegateInstalled.get()) {
			log.info("Preparing delegate...");
			delegateInstalled.getAndSet(true);
			autowireCapableBeanFactory.autowireBean(delegate);
		}
		delegate.moduleStart();
		registerAppender();
	}

	/**
	 * To register appender after built
	 */
	public void registerAppender() {
		log.info("Starting module.");
		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		LoggerContext loggerContext = rootLogger.getLoggerContext();

		super.setContext(loggerContext);
		super.setName("module-logger");
		super.start();

		eventLayout.setContext(loggerContext);
		eventLayout.start();

		rootLogger.addAppender(this);
		log.info("Started module.");
	}

	/**
	 * To stop module activity
	 */
	@Override
	public void moduleStop() {
		unRegisterAppender();
		delegate.moduleStop();
	}

	/**
	 * To unregister appender before will destroyed
	 */
	public void unRegisterAppender() {
		log.info("Stopping module.");

		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		eventLayout.start();
		rootLogger.detachAppender(this);
		super.stop();
		log.info("Stopped module.");
	}


	@Override
	protected void append(ILoggingEvent event) {
		final ModuleAction currentAction = actionFactory.currentAction();
		if (Objects.isNull(currentAction)) {
			return;
		}
		final String message = eventLayout.doLayout(event).trim();
		final ModuleMetric loggerMetric = new Metric(currentAction, timeService.now(), message);
		modulesRegistry.getRegistered(currentAction.getModule()).getMetricsContainer().add(loggerMetric);
	}

	/**
	 * To setup severity level of logging
	 *
	 * @param level new value of level
	 */
	@Override
	public void serSeverityLevel(Level level) {
		final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(level);
	}

	/**
	 * To setup value of layout's pattern
	 *
	 * @param layoutPattern  pattern for appender layout
	 */
	@Override
	public void setLayoutPattern(String layoutPattern) {
		((PatternLayout) eventLayout).setPattern(layoutPattern);
	}

	/**
	 * To check is module active (is working)
	 *
	 * @return true if module is working
	 */
	@Override
	public boolean isActive() {
		return delegate.isActive();
	}

	/**
	 * To get the registry condition of module for the moment
	 *
	 * @return current condition value
	 */
	@Override
	public ModuleHealthCondition getCondition() {
		return delegate.getCondition();
	}

	/**
	 * After action detected fail
	 *
	 * @param exception cause of fail
	 */
	@Override
	public void healthGoLow(Throwable exception) {
		delegate.healthGoLow(exception);
	}

	/**
	 * To get instance of last thrown exception
	 *
	 * @return exception or nul if wouldn't
	 */
	@Override
	public Throwable lastThrown() {
		return delegate.lastThrown();
	}

	/**
	 * After action detected success
	 */
	@Override
	public void healthGoUp() {
		delegate.healthGoUp();
	}

	/**
	 * To check is module allows to be restarted
	 *
	 * @return true if module can restart
	 */
	@Override
	public boolean canRestart() {
		return delegate.canRestart();
	}

	/**
	 * To get root action of module
	 *
	 * @return instance
	 */
	@Override
	public ModuleAction getMainAction() {
		return delegate.getMainAction();
	}

	/**
	 * To get access to Module's metrics container
	 *
	 * @return instance
	 */
	@Override
	public MetricsContainer getMetricsContainer() {
		return delegate.getMetricsContainer();
	}

	/**
	 * To get current configuration of module
	 *
	 * @return configuration as map
	 */
	@Override
	public Map<String, VariableItem> getConfiguration() {
		return delegate.getConfiguration();
	}

	/**
	 * Notification about change configuration
	 *
	 * @param changed map with changes
	 */
	@Override
	public void configurationChanged(Map<String, VariableItem> changed) {
		delegate.configurationChanged(changed);
	}

	// private methods
	boolean configurationItemChanged(String itemName, VariableItem itemValue) {
		final VariableItem configurationVariable;
		if (Objects.isNull(configurationVariable = delegate.configurationVariableOf(itemName))) {
			log.warn("No accessible variable '{}' in configuration.", itemName);
			return false;
		}
		// check for level
		if (itemName.equals(levelName())) {
			levelSeverity = itemValue.get(Integer.class).intValue();
			log.debug("Changed variable 'levelSeverity' to {}", levelSeverity);
			configurationVariable.set(levelSeverity);
			serSeverityLevel(Level.toLevel(levelSeverity));
			return true;
		}
		// check for layoutPattern
		if (itemName.equals(patternName())) {
			layoutPattern = itemValue.get(String.class);
			log.debug("Changed variable 'layoutPattern' to {}", layoutPattern);
			configurationVariable.set(layoutPattern);
			setLayoutPattern(layoutPattern);
			return true;
		}
		return false;
	}

	// inner classes
	static class Metric extends ModuleMetricAdapter {

		final String message;

		Metric(ModuleAction action, Instant measured, String message) {
			super(action, measured);
			this.message = message;
		}

		/**
		 * To get the name of metric
		 *
		 * @return the name
		 */
		@Override
		public String name() {
			return "logger";
		}

		/**
		 * To fill values metric's depended information
		 *
		 * @return concrete
		 */
		@Override
		protected String concreteValue() {
			return message;
		}

		@Override
		public String toString() {
			return "LoggerMetric{" +
					"action='" + super.action().getName() + '\'' +
					" time='" + super.measured() + '\'' +
					" message='" + message + '\'' +
					'}';
		}
	}

	class ServiceDelegate extends ModuleServiceAdapter implements ModuleLoggerService {

		/**
		 * To setup severity level of logging
		 *
		 * @param level new value of level
		 */
		@Override
		public void serSeverityLevel(Level level) {
			throw new IllegalStateException("Not realized here.");
		}

		/**
		 * To setup value of layout's pattern
		 *
		 * @param layoutPattern pattern for appender layout
		 */
		@Override
		public void setLayoutPattern(String layoutPattern) {
			throw new IllegalStateException("Not realized here.");
		}

		/**
		 * To restart module
		 */
		@Override
		public void restart() {
			ModuleSlf4jLogAppender.this.restart();
		}

		/**
		 * Notify about change in configuration variable
		 *
		 * @param itemName  name of property
		 * @param itemValue new value of property
		 * @return true if made change
		 */
		@Override
		protected boolean configurationItemChanged(String itemName, VariableItem itemValue) {
			return ModuleSlf4jLogAppender.this.configurationItemChanged(itemName, itemValue);
		}
	}
}
