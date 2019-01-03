/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;

/**
 * Service: container of metrics for module (Health State)
 *
 * @see oleg.sopilnyak.module.metric.MetricsContainer
 */
public interface HeartBeatMetricContainer extends MetricsContainer {
	/**
	 * To store Health Condition for module
	 *
	 * @param action action to save
	 * @param module module to check
	 */
	void heatBeat(ModuleAction action, Module module);
}