/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.bean;

import oleg.sopilnyak.service.metric.ModuleMetricAdapter;

/**
 * Type : metric for action changed
 */
public class ActionChangedMetric extends ModuleMetricAdapter {

	@Override
	public String getName() {
		return "changed-action-state to {" + this.getAction().getState() + "} of " + this.getAction().getName();
	}

	@Override
	protected String concreteValue() {
		return "";
	}
}
