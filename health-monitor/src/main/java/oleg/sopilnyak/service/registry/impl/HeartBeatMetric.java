/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.registry.impl;

import lombok.Data;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.metric.impl.ModuleMetricAdapter;
import oleg.sopilnyak.service.registry.ModulesRegistry;

import java.time.Instant;

/**
 * Type: DTO for module's registry state metric
 */
@Data
public class HeartBeatMetric extends ModuleMetricAdapter {
	public static final String NAME = "heart-beat of ";
	public static final long DELAY = ModulesRegistry.DELAY_DEFAULT;

	private final ModuleHealthCondition condition;
	private final boolean active;
	private final String modulePK;

	public HeartBeatMetric(ModuleAction action, Module module, Instant measured) {
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
