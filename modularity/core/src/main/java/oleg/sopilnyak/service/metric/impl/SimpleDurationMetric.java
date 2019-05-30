/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;


import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;

import java.time.Instant;

/**
 * Type : metric module activity duration (action context)
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

	@Override
	public String getName() {
		return "simple-duration of '" + label + "' for :" + module;
	}

	@Override
	protected String concreteValue() {
		return label + " of module: " + module + " lasted " + duration + " millis.";
	}
}
