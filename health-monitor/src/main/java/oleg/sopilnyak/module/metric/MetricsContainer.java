/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.metric;

import oleg.sopilnyak.module.model.ModuleAction;

import java.util.Collection;

/**
 * Service container of metrics for module
 */
public interface MetricsContainer {
	/**
	 * To add module's metric
	 *
	 * @param metric to add
	 */
	void add(ModuleMetric metric);

	/**
	 * To add set of module's metrics
	 *
	 * @param metrics to add
	 */
	void add(Collection<ModuleMetric> metrics);

	/**
	 * To clear container
	 */
	void clear();

	/**
	 * To get module's metrics snapshot and clear internal metrics set
	 *
	 * @return metrics snapshot
	 */
	Collection<ModuleMetric> metrics();

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
	 * @param t exception
	 */
	void actionFinished(ModuleAction action, Throwable t);

	/**
	 * To add metric about action finish
	 *
	 * @param action action
	 */
	void actionFinished(ModuleAction action);
}
