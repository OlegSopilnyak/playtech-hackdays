/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.metric.impl.ModuleMetricAdapter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
	private TimeService timeService;

	@PostConstruct
	public void registerAppender(){
		super.start();

		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		LoggerContext loggerContext = rootLogger.getLoggerContext();
		super.setContext(loggerContext);
		super.setName("module-logger");

		rootLogger.addAppender(this);
		// we are not interested in auto-configuration
		loggerContext.reset();


	}
	@PreDestroy
	public void unRegisterAppender(){
		super.stop();
		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.detachAppender(this);
	}
	@Override
	protected void append(ILoggingEvent event) {
		final ModuleAction currentAction = actionFactory.currentAction();
		if (Objects.isNull(currentAction)){
			return;
		}
		final String message = eventLayout.doLayout(event);
		final ModuleMetric loggerMetric = new Metric(currentAction, timeService.now(), message);
		((Module)currentAction.getModule()).getMetricsContainer().add(loggerMetric);
	}
	// inner classes
	class Metric extends ModuleMetricAdapter{

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
