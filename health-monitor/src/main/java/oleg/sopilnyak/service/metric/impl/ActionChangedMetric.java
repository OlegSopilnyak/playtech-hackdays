/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.model.ModuleAction;

import java.time.Instant;

/**
 * Type : metric for action changed
 */
class ActionChangedMetric extends ModuleMetricAdapter {
	public ActionChangedMetric(ModuleAction action, Instant now) {
		super(action, now);
	}

	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	@Override
	public String name() {
		return "changed-action-state to {"+action().getState()+"} of " + action().getName();
	}

	/**
	 * To fill values metric's depended information
	 *
	 * @return concrete
	 */
	@Override
	protected String concreteValue() {
		return "";
	}
}
