/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.dto;

import lombok.Data;
import oleg.sopilnyak.module.metric.*;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * Type: DTO type of module metrics container
 */
@Data
public class MetricContainerDto implements MetricsContainer {
	private Collection<ModuleMetric> metrics;

	/**
	 * To add module's metric
	 *
	 * @param metric to add
	 */
	@Override
	public void add(ModuleMetric metric) {
		throw new UnsupportedOperationException("Operation not allowed for transport object");
	}

	/**
	 * To add set of module's metrics
	 *
	 * @param metrics to add
	 */
	@Override
	public void add(Collection<ModuleMetric> metrics) {
		throw new UnsupportedOperationException("Operation not allowed for transport object");
	}

	/**
	 * The quantity of unprocessed metrics
	 *
	 * @return metrics in container
	 */
	@Override
	public int unProcessed() {
		return CollectionUtils.isEmpty(metrics) ? 0 : metrics.size();
	}

	/**
	 * To clear container
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException("Operation not allowed for transport object");
	}

	/**
	 * To get module's metrics snapshot and clear internal metrics set
	 *
	 * @return metrics snapshot
	 */
	@Override
	public Collection<ModuleMetric> metrics() {
		return Collections.unmodifiableCollection(metrics);
	}

	/**
	 * To get reference to action-metrics container
	 *
	 * @return reference
	 */
	@Override
	public ActionMetricsContainer action() {
		throw new UnsupportedOperationException("Operation not allowed for transport object");
	}

	/**
	 * To get reference to health-metrics container
	 *
	 * @return reference
	 */
	@Override
	public HeartBeatMetricContainer health() {
		throw new UnsupportedOperationException("Operation not allowed for transport object");
	}

	/**
	 * To get reference to duration-metrics container
	 *
	 * @return reference
	 */
	@Override
	public DurationMetricsContainer duration() {
		throw new UnsupportedOperationException("Operation not allowed for transport object");
	}
}
