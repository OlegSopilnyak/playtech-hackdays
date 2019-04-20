/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;

import java.time.Instant;

/**
 * Type: metric for heart beat
 */
class HeartBeatMetric extends ModuleMetricAdapter {
	private static final String NAME = "heart-beat of ";

	private final ModuleHealthCondition condition;
	private final boolean active;
	private final String modulePK;

	HeartBeatMetric(ModuleAction action, Module module, Instant measured) {
		super(action, measured);
		this.condition = module.getCondition();
		active = module.isActive();
		modulePK = module.primaryKey();
	}

	@Override
	public Object[] value() {
		return new Object[]{active, condition};
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	protected String concreteValue() {
		return "module: " + modulePK + ", condition: " + condition + ", active: " + active;
	}
}

