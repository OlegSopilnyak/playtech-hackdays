/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Instant;

/**
 * Configuration for metrics types
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
public class ModuleMetricsConfiguration {
	@Bean(name = "action-changed")
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ActionChangedMetric getActionChangedMetric(ModuleAction action, Instant mark) {
		return new ActionChangedMetric(action, mark);
	}

	@Bean(name = "action-fail")
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ActionExceptionMetric getActionExceptionMetric(ModuleAction action, Instant mark, Throwable cause) {
		return new ActionExceptionMetric(action, mark, cause);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public HeartBeatMetric getHeartBeatMetric(ModuleAction action, Module module, Instant measured) {
		return new HeartBeatMetric(action, module, measured);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public SimpleDurationMetric getSimpleDurationMetric(String label, ModuleAction action, Instant measured, String module, long duration) {
		return new SimpleDurationMetric(label, action, measured, module, duration);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public TotalDurationMetric getTotalDurationMetric(String label, ModuleAction action, Instant measured, int modules, long duration) {
		return new TotalDurationMetric(label, action, measured, modules, duration);
	}
}
