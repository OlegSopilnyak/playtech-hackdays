/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.metric.ActionMetricsContainer;
import oleg.sopilnyak.service.metric.DurationMetricsContainer;
import oleg.sopilnyak.service.metric.HeartBeatMetricContainer;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Service - container of metrics
 */
public class MetricsContainerImpl implements MetricsContainer, ActionMetricsContainer, HeartBeatMetricContainer, DurationMetricsContainer {
	private final Queue<ModuleMetric> metrics = new ConcurrentLinkedQueue<>();
	@Autowired
	private TimeService timeService;

	/**
	 * To add module's metric
	 *
	 * @param metric to add
	 */
	@Override
	public void add(ModuleMetric metric) {
		metrics.offer(metric);
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
		metrics.clear();
	}

	/**
	 * To get module's metrics snapshot and clear internal metrics set
	 *
	 * @return metrics snapshot
	 */
	@Override
	public Collection<ModuleMetric> metrics() {
		return metrics.stream().collect(Collectors.toList());
	}

	/**
	 * To get reference to action-metrics container
	 *
	 * @return reference
	 */
	@Override
	public ActionMetricsContainer action() {
		return this;
	}

	/**
	 * To add metric about action state change
	 *
	 * @param action action
	 */
	@Override
	public void changed(ModuleAction action) {
		final ModuleActionAdapter adapter = (ModuleActionAdapter) action;
		final Instant mark = timeService.now();
		switch (action.getState()) {
			case INIT:
				adapter.setDuration(-1L);
				break;
			case PROGRESS:
				adapter.setStarted(mark);
				adapter.setDuration(0L);
				break;

		}
		add(new ActionChangedMetric(action, mark));
	}

	/**
	 * To add metric about action finish
	 *
	 * @param action action
	 * @param t      exception
	 */
	@Override
	public void fail(ModuleAction action, Throwable t) {
		final ModuleActionAdapter adapter = (ModuleActionAdapter) action;
		adapter.setDuration(timeService.duration(action.getStarted()));
		adapter.setState(ModuleAction.State.FAIL);
		add(new ActionExceptionMetric(action, timeService.now(), t));
	}

	/**
	 * To add metric about action finish
	 *
	 * @param action action
	 */
	@Override
	public void success(ModuleAction action) {
		final ModuleActionAdapter adapter = (ModuleActionAdapter) action;
		adapter.setDuration(timeService.duration(action.getStarted()));
		adapter.setState(ModuleAction.State.SUCCESS);
		add(new ActionChangedMetric(action, timeService.now()));
	}

	/**
	 * To get reference to health-metrics container
	 *
	 * @return reference
	 */
	@Override
	public HeartBeatMetricContainer health() {
		return this;
	}

	/**
	 * To store Health Condition for module
	 *
	 * @param action action to save
	 * @param module module to check
	 */
	@Override
	public void heartBeat(ModuleAction action, Module module) {
		module.getMetricsContainer().add(new HeartBeatMetric(action, module, timeService.now()));
	}

	/**
	 * To get reference to duration-metrics container
	 *
	 * @return reference
	 */
	@Override
	public DurationMetricsContainer duration() {
		return this;
	}

	/**
	 * To register simple-duration metric of operation for module in action
	 *
	 * @param label    label of activity
	 * @param action   action-context
	 * @param measured time of occurrence
	 * @param module   pk of processed module
	 * @param duration duration of operation
	 */
	@Override
	public void simple(String label, ModuleAction action, Instant measured, String module, long duration) {
//		System.out.println("++ " + label + " simple duration metrics size :" + metrics.size());
		add(new SimpleDurationMetric(label, action, measured, module, duration));
	}

	/**
	 * To register total-duration metric of operations for modules in action
	 *
	 * @param label    label of activity
	 * @param action   action-context
	 * @param measured time of occurrence
	 * @param modules  amount of modules
	 * @param duration durations of operations
	 */
	@Override
	public void total(String label, ModuleAction action, Instant measured, int modules, long duration) {
//		System.out.println("++ " + label + " total duration metrics size : " + metrics.size());
		add(new TotalDurationMetric(label, action, measured, modules, duration));
	}
}
