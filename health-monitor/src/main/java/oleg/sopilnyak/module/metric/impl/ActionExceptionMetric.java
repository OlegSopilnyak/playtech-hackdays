/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.metric.impl;

import oleg.sopilnyak.module.model.ModuleAction;

import java.time.Instant;

/**
 * Type: metric for fail action
 */
public class ActionExceptionMetric extends ActionChangedMetric {
	private final Throwable cause;
	public ActionExceptionMetric(ModuleAction action, Instant now, Throwable cause) {
		super(action, now);
		this.cause = cause;
	}

	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	@Override
	public String name() {
		return "ModuleActionFailMetric";
	}

	/**
	 * The values of metric, according to metric's type it maybe one or several values
	 *
	 * @return array of values
	 */
	@Override
	public Object[] value() {
		return new Object[]{cause};
	}
}
