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
	public String getName() {
		return "changed-action-state to {" + this.getAction().getState() + "} of " + this.getAction().getName();
	}

	@Override
	protected String concreteValue() {
		return "";
	}
}
