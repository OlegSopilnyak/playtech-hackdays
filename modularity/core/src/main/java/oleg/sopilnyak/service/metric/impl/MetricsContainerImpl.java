/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.metric.ActionMetricsContainer;
import oleg.sopilnyak.service.metric.DurationMetricsContainer;
import oleg.sopilnyak.service.metric.HeartBeatMetricContainer;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.PrintWriter;
import java.io.StringWriter;
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
        metrics.forEach(this::add);
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
        // initialize the action
        adapter.setState(ModuleAction.State.INIT);
        this.action().changed(adapter);
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
        add(new TotalDurationMetric(label, action, measured, modules, duration));
    }

    // inner classes

    /**
     * Type : metric for action changed
     */
    private static class ActionChangedMetric extends ModuleMetricAdapter {
        private ActionChangedMetric(ModuleAction action, Instant now) {
            super(action, now);
        }

        @Override
        public String name() {
            return "changed-action-state to {" + action().getState() + "} of " + action().getName();
        }

        @Override
        protected String concreteValue() {
            return "";
        }
    }

    /**
     * Type: metric for fail action
     */
    private static class ActionExceptionMetric extends ActionChangedMetric {
        private final Throwable cause;

        private ActionExceptionMetric(ModuleAction action, Instant now, Throwable cause) {
            super(action, now);
            this.cause = cause;
        }

        @Override
        public String name() {
            return "exception in '" + action().getName() + "'";
        }

        @Override
        public Object[] value() {
            return new Object[]{cause};
        }

        @Override
        protected String concreteValue() {
            if (cause != null) {
                final StringWriter message = new StringWriter();
                try (PrintWriter out = new PrintWriter(message, true)) {
                    cause.printStackTrace(out);
                }
                return message.toString();
            } else {
                return super.concreteValue();
            }
        }
    }

    /**
     * Type: metric for heart beat
     */
    private static class HeartBeatMetric extends ModuleMetricAdapter {
        private static final String NAME = "heart-beat of ";

        private final ModuleHealthCondition condition;
        private final boolean active;
        private final String modulePK;

        private HeartBeatMetric(ModuleAction action, Module module, Instant measured) {
            super(action, measured);
            this.condition = module.getCondition();
            active = module.isActive();
            modulePK = module.primaryKey();
        }

        @Override
        public Object[] value() {
            return new Object[]{active, condition};
        }

        @Override
        public String name() {
            return NAME;
        }

        @Override
        protected String concreteValue() {
            return "module: " + modulePK + ", condition: " + condition + ", active: " + active;
        }
    }

    /**
     * Type : metric module activity duration (action context)
     */
    private static class SimpleDurationMetric extends ModuleMetricAdapter {
        private final String label;
        private final String module;
        private final long duration;

        private SimpleDurationMetric(String label, ModuleAction action, Instant measured, String module, long duration) {
            super(action, measured);
            this.label = label;
            this.module = module;
            this.duration = duration;
        }

        @Override
        public String name() {
            return "simple-duration of '" + label + "' for :" + module;
        }

        @Override
        protected String concreteValue() {
            return label + " of module: " + module + " lasted " + duration + " millis.";
        }
    }
    /**
     * Type : metric for module check health
     */
    private static class TotalDurationMetric extends ModuleMetricAdapter {
        private final String label;
        private final int modules;
        private final long duration;

        private TotalDurationMetric(String label, ModuleAction action, Instant measured, int modules, long duration) {
            super(action, measured);
            this.label = label;
            this.modules = modules;
            this.duration = duration;
        }

        @Override
        public String name() {
            return "total-duration of '" + label + "' for " + modules + " modules";
        }

        @Override
        protected String concreteValue() {
            return label + " of " + modules + " modules lasted " + duration + " millis";
        }
    }
}
