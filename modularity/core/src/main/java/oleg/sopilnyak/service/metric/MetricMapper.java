/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.metric.bean.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

/**
 * MapStruct mapper for various metrics
 */
@Mapper
public interface MetricMapper {
	MetricMapper INSTANCE = Mappers.getMapper(MetricMapper.class);

	/**
	 * Create action-changed module-metric
	 *
	 * @param action changed action
	 * @param measured time when it is occurred
	 * @return instance
	 */
	@Mapping(target = "action", expression = "java(action)" )
	@Mapping(target = "measured", source = "measured")
	ActionChangedMetric toActionChanged(ModuleAction action, Instant measured);

	/**
	 * Create action-failed module-metric
	 *
	 * @param action failed action
	 * @param measured time when it is occurred
	 * @param cause what happened
	 * @return instance
	 */
	@Mapping(target = "action", expression = "java(action)" )
	@Mapping(target = "measured", source = "measured")
	@Mapping(target = "cause", source = "cause")
	ActionExceptionMetric toActionFailed(ModuleAction action, Instant measured, Throwable cause);

	/**
	 * Create heart-beat module-metric
	 *
	 * @param action  action owner of the metric
	 * @param module module to check
	 * @param measured time when it is occurred
	 * @return instance
	 */
	@Mapping(target = "action", expression = "java(action)" )
	@Mapping(target = "measured", source = "measured")
	@Mapping(target = "condition", source = "module.condition")
	@Mapping(target = "active", source = "module.active")
	@Mapping(target = "modulePK", expression = "java(module.primaryKey())")
	HeartBeatMetric toHeartBeat(ModuleAction action, Module module, Instant measured);

	/**
	 * Create simple-duration metric for an action
	 *
	 * @param label the label of action
	 * @param action action owner of metric
	 * @param measured time when it occurred
	 * @param modulePK owner of the action
	 * @param duration duration of action's activity
	 * @return instance
	 */
	@Mapping(target = "action", expression = "java(action)" )
	@Mapping(target = "measured", source = "measured")
	@Mapping(target = "label", source = "label")
	@Mapping(target = "modulePK", source = "modulePK")
	@Mapping(target = "duration", source = "duration")
	SimpleDurationMetric simpleDuration(String label, ModuleAction action, Instant measured, String modulePK, long duration);

	/**
	 * Create total-duration metric for the action
	 *
	 * @param label the label of action
	 * @param action action owner of metric
	 * @param measured time when it occurred
	 * @param modules quantity of actions processed by
	 * @param duration duration of action's activity
	 * @return instance
	 */
	@Mapping(target = "action", expression = "java(action)" )
	@Mapping(target = "measured", source = "measured")
	@Mapping(target = "label", source = "label")
	@Mapping(target = "modules", source = "modules")
	@Mapping(target = "duration", source = "duration")
	TotalDurationMetric totalDuration(String label, ModuleAction action, Instant measured, int modules, long duration);

	/**
	 * Create logger-message metric
	 *
	 * @param action action owner of metric
	 * @param measured time when it occurred
	 * @param message log-message
	 * @return instance
	 */
	@Mapping(target = "action", expression = "java(action)" )
	@Mapping(target = "measured", source = "measured")
	@Mapping(target = "message", source = "message")
	Slf4jLogMetric toLog(ModuleAction action, Instant measured, String message);
}
