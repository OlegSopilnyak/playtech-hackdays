/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.metric.impl;

import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service - container of metrics
 */
public class MetricsContainerImpl implements MetricsContainer {
	private static final Object METRIC = new Object();
	private ConcurrentMap<ModuleMetric, Object> metrics = new ConcurrentHashMap<>();
	@Autowired
	private TimeService timeService;

	/**
	 * To add module's metric
	 *
	 * @param metric to add
	 */
	@Override
	public void add(ModuleMetric metric) {
		metrics.put(metric, METRIC);
	}

	/**
	 * To add set of module's metrics
	 *
	 * @param metrics to add
	 */
	@Override
	public void add(Collection<ModuleMetric> metrics) {
		metrics.forEach(m -> this.add(m));
	}

	/**
	 * To clear container
	 */
	@Override
	public void clear() {
		metrics = new ConcurrentHashMap<>();
	}

	/**
	 * To get module's metrics snapshot and clear internal metrics set
	 *
	 * @return metrics snapshot
	 */
	@Override
	public Collection<ModuleMetric> metrics() {
		return metrics.keySet();
	}

	/**
	 * To add metric about action state change
	 *
	 * @param action action
	 */
	@Override
	public void actionChanged(ModuleAction action) {
		final ModuleActionAdapter adapter = (ModuleActionAdapter) action;
		switch (action.getState()) {
			case INIT:
				adapter.setDuration(-1L);
				break;
			case PROGRESS:
				adapter.setStarted(timeService.now());
				adapter.setDuration(0L);
				break;

		}
		this.add(new ActionChangedMetric(action, timeService.now()));
	}

	/**
	 * To add metric about action finish
	 *
	 * @param action action
	 * @param t      exception
	 */
	@Override
	public void actionFinished(ModuleAction action, Throwable t) {
		final ModuleActionAdapter adapter = (ModuleActionAdapter) action;
		adapter.setDuration(timeService.duration(action.getStarted()));
		this.add(new ActionExceptionMetric(action, timeService.now(), t));
	}

	/**
	 * To add metric about action finish
	 *
	 * @param action action
	 */
	@Override
	public void actionFinished(ModuleAction action) {
		final ModuleActionAdapter adapter = (ModuleActionAdapter) action;
		adapter.setDuration(timeService.duration(action.getStarted()));
		this.add(new ActionChangedMetric(action, timeService.now()));
	}
}
