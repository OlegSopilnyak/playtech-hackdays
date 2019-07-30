/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.bean;


import lombok.Data;
import lombok.EqualsAndHashCode;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;

/**
 * Type : metric module activity duration (action context)
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class SimpleDurationMetric extends ModuleMetricAdapter {
	private String label;
	private String modulePK;
	private long duration;


	@Override
	public String getName() {
		return "simple-duration of '" + label + "' for :" + modulePK;
	}

	@Override
	protected String concreteValue() {
		return label + " of module: " + modulePK + " lasted " + duration + " millis.";
	}
}
