/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.impl;

import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.metric.impl.ModuleMetricAdapter;

import java.time.Instant;

/**
 * Type : metric for module check health
 */
class SimpleDurationMetric extends ModuleMetricAdapter {
	private final String module;
	private final long duration;

	public SimpleDurationMetric(ModuleAction action, Instant measured, String module, long duration) {
		super(action, measured);
		this.module = module;
		this.duration = duration;
	}

	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	@Override
	public String name() {
		return "module-configuration";
	}

	/**
	 * To fill values metric's depended information
	 *
	 * @return concrete
	 */
	@Override
	protected String concreteValue() {
		return "Module:" + module + " configuration lasted " + duration + " millis.";
	}
}
