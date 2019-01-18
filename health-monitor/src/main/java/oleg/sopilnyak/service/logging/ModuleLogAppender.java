/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.metric.impl.ModuleMetricAdapter;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Objects;

/**
 * Service: appender for slf4j logging framework
 */
public class ModuleLogAppender extends AppenderBase<ILoggingEvent> {

	@Autowired
	private ModuleActionFactory actionFactory;
	@Autowired
	private Layout<ILoggingEvent> eventLayout;
	@Autowired
	private ModulesRegistry modulesRegistry;
	@Autowired
	private TimeService timeService;

	/**
	 * To register appender after built
	 */
	public void registerAppender() {

		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		LoggerContext loggerContext = rootLogger.getLoggerContext();
		// we are not interested in auto-configuration
//		loggerContext.reset();

		super.setContext(loggerContext);
		super.setName("module-logger");
		super.start();

		eventLayout.setContext(loggerContext);
		eventLayout.start();

		rootLogger.addAppender(this);

	}

	/**
	 * To unregister appender before will destroyed
	 */
	public void unRegisterAppender() {
		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		eventLayout.start();
		rootLogger.detachAppender(this);
		super.stop();
	}


	@Override
	protected void append(ILoggingEvent event) {
		final ModuleAction currentAction = actionFactory.currentAction();
		if (Objects.isNull(currentAction)) {
			return;
		}
		final String message = eventLayout.doLayout(event).trim();
		final ModuleMetric loggerMetric = new Metric(currentAction, timeService.now(), message);
//		modulesRegistry.getRegistered(currentAction.getModule()).getMetricsContainer().add(loggerMetric);
	}

	// inner classes
	class Metric extends ModuleMetricAdapter {

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
}
