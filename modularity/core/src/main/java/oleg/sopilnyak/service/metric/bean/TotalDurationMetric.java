/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;

/**
 * Type : metric for module check health
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class TotalDurationMetric extends ModuleMetricAdapter {
	private String label;
	private int modules;
	private long duration;

	@Override
	public String getName() {
		return "total-duration of '" + label + "' for " + modules + " modules";
	}

	@Override
	protected String concreteValue() {
		return label + " of " + modules + " modules lasted " + duration + " millis";
	}
}
