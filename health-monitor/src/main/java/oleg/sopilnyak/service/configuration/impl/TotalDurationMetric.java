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
class TotalDurationMetric extends ModuleMetricAdapter {
	private final int modules;
	private final long duration;

	public TotalDurationMetric(ModuleAction action, Instant measured, int modules, long duration) {
		super(action, measured);
		this.modules = modules;
		this.duration = duration;
	}

	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	@Override
	public String name() {
		return "total-modules-configuration";
	}

	/**
	 * To fill values metric's depended information
	 *
	 * @return concrete
	 */
	@Override
	protected String concreteValue() {
		return "Configuration of " + modules + " modules lasted " + duration + " millis.";
	}
}
