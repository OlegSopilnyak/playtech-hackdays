/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleValues;
import oleg.sopilnyak.module.metric.*;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.ActionContext;
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
@Slf4j
public class MetricsContainerImpl implements MetricsContainer, ActionMetricsContainer, HeartBeatMetricContainer, DurationMetricsContainer {
	// local container of metrics
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
		assert metric != null : "Metric cannot be null.";
		log.debug("Adding {}", metric);
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
		log.debug("Adding {}", metrics);
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
		log.debug("Cleaning container.");
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
		log.debug("Changing {}", action);
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
		log.debug("Failing {} by {}", action, t.getMessage());
		final ModuleActionAdapter adapter = (ModuleActionAdapter) action;
		adapter.setDuration(timeService.duration(action.getStarted()));
		adapter.setState(ModuleAction.State.FAIL);

		// make and store metric instance
		add(MetricMapper.INSTANCE.toActionFailed(action, timeService.now(), t));

		// initialize the action
		log.debug("Restore state of {} to INIT", adapter);
		adapter.setState(ModuleAction.State.INIT);
		action().changed(adapter);
	}

	/**
	 * To add metric about action finish
	 *
	 * @param action action
	 */
	@Override
	public void success(ModuleAction action) {
		log.debug("Success for {}", action);
		final ModuleActionAdapter adapter = (ModuleActionAdapter) action;
		adapter.setDuration(timeService.duration(action.getStarted()));
		adapter.setState(ModuleAction.State.SUCCESS);

		// make and store metric instance
		add(MetricMapper.INSTANCE.toActionChanged(action, timeService.now()));

		// initialize the action
		log.debug("Restore state of {} to INIT", adapter);
		adapter.setState(ModuleAction.State.INIT);
		action().changed(adapter);
	}

	/**
	 * To save metric about start action with parameters
	 *
	 * @param action  regular module-action
	 * @param context input parameters of the action
	 */
	@Override
	public void start(ModuleAction action, ActionContext context) {
		if (context.isTrivial()){
			log.debug("Context is trivial {}", context);
			return;
		}
		// make and store metric instance
		add(MetricMapper.INSTANCE.toActionStart(action, timeService.now(), context));
	}

	/**
	 * To save metric about finish action with parameters
	 *
	 * @param action regular module-action
	 * @param output result of the action
	 */
	@Override
	public void finish(ModuleAction action, Object output) {
		if (Objects.isNull(output)){
			log.debug("No output for action.");
			return;
		}
		// make and store metric instance
		add(MetricMapper.INSTANCE.toActionFinish(action, timeService.now(), output));
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
		final String modulePK = module.primaryKey();
		log.debug("Saving heart-beat for {}", modulePK);
		final MetricsContainer metricsContainer = module.getMetricsContainer();
		final Instant measured = timeService.now();
		final ModuleValues.Visitor valuesVisitor = new ModuleValues.Visitor() {
			public void visit(ModuleValues values) {
				log.debug("Saving hear-beat for host: '{}'", values.getHost());
				// make and store metric instance to module's metrics container
				final ModuleMetric metric = MetricMapper.INSTANCE.toHeartBeat(modulePK, action, values, measured);
				metricsContainer.add(metric);
			}
		};
		module.accept(valuesVisitor);
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
	 * To merge content of container with another one
	 *
	 * @param container other container
	 */
	@Override
	public void merge(MetricsContainer container) {
		container.metrics().forEach(m -> metrics.add(m));
	}

	/**
	 * To register simple-duration metric of operation for module in action
	 *
	 * @param label    label of activity
	 * @param action   action-context
	 * @param measured time of occurrence
	 * @param modulePK   pk of processed module
	 * @param duration duration of operation
	 */
	@Override
	public void simple(String label, ModuleAction action, Instant measured, String modulePK, long duration) {
		log.debug("Saving simple duration '{}' for {}", label, modulePK);
		// make and store metric instance
		add(MetricMapper.INSTANCE.simpleDuration(label, action, measured, modulePK, duration));
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
		log.debug("Saving total duration '{}' for {} modules.", label, modules);
		// make and store metric instance
		add(MetricMapper.INSTANCE.totalDuration(label, action, measured, modules, duration));
	}

}
