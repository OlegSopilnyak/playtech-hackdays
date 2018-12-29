/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.metric;

/**
 * Type metric for module's log feature
 */
public interface LogRecordMetric extends ModuleMetric {
	String NAME = "log.record";
	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	@Override
	default String name(){
		return NAME;
	}
}
