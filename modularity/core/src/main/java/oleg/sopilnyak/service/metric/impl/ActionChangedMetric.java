/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;

import java.time.Instant;

/**
 * Type : metric for action changed
 */
class ActionChangedMetric extends ModuleMetricAdapter {
	ActionChangedMetric(ModuleAction action, Instant now) {
		super(action, now);
	}

	@Override
	public String name() {
		return "changed-action-state to {" + action().getState() + "} of " + action().getName();
	}

	@Override
	protected String concreteValue() {
		return "";
	}
}
