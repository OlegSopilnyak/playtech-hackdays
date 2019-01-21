/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.model.ModuleAction;

import java.time.Instant;

/**
 * Type : metric for module check health
 */
class TotalDurationMetric extends ModuleMetricAdapter {
	private final String label;
	private final int modules;
	private final long duration;

	TotalDurationMetric(String label, ModuleAction action, Instant measured, int modules, long duration) {
		super(action, measured);
		this.label = label;
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
		return "total-duration of " + label + " for " + modules + " modules";
	}

	/**
	 * To fill values metric's depended information
	 *
	 * @return concrete
	 */
	@Override
	protected String concreteValue() {
		return label + " of " + modules + " modules lasted " + duration + " millis";
	}
}
