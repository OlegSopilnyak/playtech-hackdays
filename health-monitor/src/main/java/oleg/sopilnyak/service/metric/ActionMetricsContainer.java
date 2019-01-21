/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric;

import oleg.sopilnyak.module.model.ModuleAction;

/**
 * Service: container of metrics for module (actions)
 *
 * @see oleg.sopilnyak.module.metric.MetricsContainer
 */
public interface ActionMetricsContainer {
	/**
	 * To add metric about action state change
	 *
	 * @param action action
	 */
	void changed(ModuleAction action);

	/**
	 * To add metric about action finish not successfully
	 *
	 * @param action action
	 * @param t      occurred exception
	 */
	void fail(ModuleAction action, Throwable t);

	/**
	 * To add metric about action finish successfully
	 *
	 * @param action action
	 */
	void success(ModuleAction action);
}
