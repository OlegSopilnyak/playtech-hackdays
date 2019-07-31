/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;

/**
 * Type: metric for heart beat
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class HeartBeatMetric extends ModuleMetricAdapter {
	private static final String NAME = "heart-beat of ";

	private ModuleHealthCondition condition;
	private boolean active;
	private String modulePK;

	@Override
	public Object[] getValue() {
		return new Object[]{active, condition};
	}

	@Override
	public String getName() {
		return NAME + modulePK;
	}

	@Override
	protected String concreteValue() {
		return "{module: \"" + modulePK + "\", condition: \"" + condition + "\", active: \"" + active+"\"}";
	}
}

