/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.metric;

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
	 * The quantity of unprocessed metrics
	 *
	 * @return metrics in container
	 */
	int unProcessed();

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
	 * To get reference to action-metrics container
	 *
	 * @return reference
	 */
	ActionMetricsContainer action();

	/**
	 * To get reference to health-metrics container
	 *
	 * @return reference
	 */
	HeartBeatMetricContainer health();


	/**
	 * To get reference to duration-metrics container
	 *
	 * @return reference
	 */
	DurationMetricsContainer duration();

	/**
	 * To merge content of container with another one
	 *
	 * @param metrics other container
	 */
	void merge(MetricsContainer metrics);
}
