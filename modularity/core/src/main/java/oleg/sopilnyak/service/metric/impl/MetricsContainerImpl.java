/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.*;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;
import oleg.sopilnyak.service.metric.MetricMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Service - container of metrics
 */
public class MetricsContainerImpl implements MetricsContainer, ActionMetricsContainer, HeartBeatMetricContainer, DurationMetricsContainer {
	// local container of metrics
	private final Queue<ModuleMetric> metrics = new ConcurrentLinkedQueue<>();

	@Autowired
	private TimeService timeService;

//	@Autowired
//	@Qualifier("action-changed")
//	private ObjectProvider<ActionChangedMetric> actionChanged;
//	@Autowired
//	@Qualifier("action-fail")
//	private ObjectProvider<ActionExceptionMetric> actionFail;
//	@Autowired
//	private ObjectProvider<HeartBeatMetric> heartBeat;
//	@Autowired
//	private ObjectProvider<SimpleDurationMetric> simpleDuration;
//	@Autowired
//	private ObjectProvider<TotalDurationMetric> totalDuration;

	/**
	 * To add module's metric
	 *
	 * @param metric to add
	 */
	@Override
	public void add(ModuleMetric metric) {
		assert metric != null : "Metric cannot be null.";
		metrics.offer(metric);
	}

	/**
	 * To add set of module's metrics
	 *
	 * @param metrics to add
	 */
	@Override
	public void add(Collection<ModuleMetric> metrics) {
		assert metrics != null : "Metrics collection cannot be null.";
		this.metrics.addAll(metrics);
	}

	/**
	 * The quantity of unprocessed metrics
	 *
	 * @return metrics in container
	 */
	@Override
	public int unProcessed() {
		return metrics.size();
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
				adapter.setStarted(null);
				adapter.setDuration(-1L);
				break;
			case PROGRESS:
				if (Objects.isNull(action.getStarted())) {
					adapter.setStarted(mark);
					adapter.setDuration(0L);
				} else {
					adapter.setDuration(timeService.duration(action.getStarted()));
				}
				break;

		}
		add(MetricMapper.INSTANCE.toActionChanged(action, mark));
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
		// make and store metric instance
		final ModuleMetric metric = MetricMapper.INSTANCE.toActionFailed(action, timeService.now(), t);
		this.add(metric);
		// initialize the action
		adapter.setState(ModuleAction.State.INIT);
		this.changed(adapter);
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
		// make and store metric instance
		final ModuleMetric metric = MetricMapper.INSTANCE.toActionChanged(action, timeService.now());
		this.add(metric);
		// initialize the action
		adapter.setState(ModuleAction.State.INIT);
		this.action().changed(adapter);
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
		// make and store metric instance to module
		final ModuleMetric metric = MetricMapper.INSTANCE.toHeartBeat(action, module, timeService.now());
		module.getMetricsContainer().add(metric);
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
		// make and store metric instance
		final ModuleMetric metric = MetricMapper.INSTANCE.simpleDuration(label, action, measured, module, duration);
		this.add(metric);
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
		// make and store metric instance
		final ModuleMetric metric = MetricMapper.INSTANCE.totalDuration(label, action, measured, modules, duration);
		this.add(metric);
	}

}
