/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.metric;

import oleg.sopilnyak.module.model.ModuleAction;

import java.time.Instant;

/**
 * Type: metric of module
 */
public interface ModuleMetric {
	/**
	 * To get action-owner of metrics
	 *
	 * @return reference to action
	 */
	ModuleAction action();

	/**
	 * To get time when metric was took
	 *
	 * @return the time
	 */
	Instant measured();

	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	String name();

	/**
	 * The values of metric, according to metric's type it maybe one or several values
	 *
	 * @return array of values
	 */
	default Object[] value(){
		return new Object[0];
	}

	/**
	 * To compose string from array of values according to metric's type
	 *
	 * @return representation of value as string
	 */
	default String valuesAsString(){
		return "";
	}
}
