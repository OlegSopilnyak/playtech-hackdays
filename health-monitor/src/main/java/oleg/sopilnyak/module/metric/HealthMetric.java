/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.metric;

import oleg.sopilnyak.module.model.ModuleHealthCondition;

/**
 * Type metric for module's health check
 */
public interface HealthMetric extends ModuleMetric {
	String NAME = "health";
	long DELAY = 2000L;
	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	@Override
	default String name(){
		return NAME;
	}

	/**
	 * The values of metric, according to metric's type it maybe one or several values
	 *
	 * @return array of values
	 */
	@Override
	default Object[] value(){
		return new Object[]{getCondition()};
	}

	/**
	 * To get the health condition of module for the moment
	 *
	 * @return current condition value
	 */
	ModuleHealthCondition getCondition();

}
