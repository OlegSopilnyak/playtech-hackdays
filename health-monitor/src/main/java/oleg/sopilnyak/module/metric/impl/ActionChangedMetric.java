/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.metric.impl;

import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;

import java.time.Instant;

/**
 * Type : metric for action changed
 */
public class ActionChangedMetric implements ModuleMetric {
	private ModuleAction action;
	private Instant measured;

	public ActionChangedMetric(ModuleAction action, Instant now) {
		this.action = action;
		measured = now;
	}

	/**
	 * To get action-owner of metrics
	 *
	 * @return reference to action
	 */
	@Override
	public ModuleAction action() {
		return action;
	}

	/**
	 * To get time when metric was took
	 *
	 * @return the time
	 */
	@Override
	public Instant measured() {
		return measured;
	}

	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	@Override
	public String name() {
		return "ModuleActionMetric";
	}
}
