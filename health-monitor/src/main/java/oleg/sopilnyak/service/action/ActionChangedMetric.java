/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action;

import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.metric.impl.ModuleMetricAdapter;

import java.time.Instant;

/**
 * Type : metric for action changed
 */
public class ActionChangedMetric extends ModuleMetricAdapter {
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
		return "action";
	}
}
