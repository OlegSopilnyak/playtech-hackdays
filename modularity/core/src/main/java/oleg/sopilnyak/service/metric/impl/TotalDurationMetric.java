/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;

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

	@Override
	public String getName() {
		return "total-duration of '" + label + "' for " + modules + " modules";
	}

	@Override
	protected String concreteValue() {
		return label + " of " + modules + " modules lasted " + duration + " millis";
	}
}
