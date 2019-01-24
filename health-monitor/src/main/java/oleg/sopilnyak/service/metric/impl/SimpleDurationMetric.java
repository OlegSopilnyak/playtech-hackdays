/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.model.ModuleAction;

import java.time.Instant;

/**
 * Type : metric for module check health
 */
class SimpleDurationMetric extends ModuleMetricAdapter {
	private final String label;
	private final String module;
	private final long duration;

	SimpleDurationMetric(String label, ModuleAction action, Instant measured, String module, long duration) {
		super(action, measured);
		this.label = label;
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
		return "simple-duration of '" + label + "' for :" + module;
	}

	/**
	 * To fill values metric's depended information
	 *
	 * @return concrete
	 */
	@Override
	protected String concreteValue() {
		return label +" of module: " + module + " lasted " + duration + " millis.";
	}
}
