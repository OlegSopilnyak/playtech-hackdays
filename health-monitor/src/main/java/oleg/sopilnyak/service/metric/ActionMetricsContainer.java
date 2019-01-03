/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric;

import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;

/**
 * Service: container of metrics for module (actions)
 *
 * @see oleg.sopilnyak.module.metric.MetricsContainer
 */
public interface ActionMetricsContainer extends MetricsContainer {
	/**
	 * To add metric about action state change
	 *
	 * @param action action
	 */
	void actionChanged(ModuleAction action);

	/**
	 * To add metric about action finish
	 *
	 * @param action action
	 * @param t      exception
	 */
	void actionFinished(ModuleAction action, Throwable t);

	/**
	 * To add metric about action finish
	 *
	 * @param action action
	 */
	void actionFinished(ModuleAction action);
}
