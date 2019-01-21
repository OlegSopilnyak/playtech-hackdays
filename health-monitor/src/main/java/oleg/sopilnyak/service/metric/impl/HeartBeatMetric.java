/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;

import java.time.Instant;

/**
 * Type: DTO for module's registry state metric
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

	/**
	 * The values of metric, according to metric's type it maybe one or several values
	 *
	 * @return array of values
	 */
	@Override
	public Object[] value() {
		return new Object[]{active, condition};
	}

	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	@Override
	public String name() {
		return NAME;
	}

	/**
	 * To fill values metric's depended information
	 *
	 * @return concrete
	 */
	@Override
	protected String concreteValue() {
		return "module: " + modulePK + ", condition: " + condition + ", active: " + active;
	}
}
