/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric;

import oleg.sopilnyak.module.ModuleValues;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.action.ActionContext;
import oleg.sopilnyak.service.metric.bean.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

/**
 * MapStruct mapper for various metrics
 */
@Mapper(imports = {ActionContext.class})
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
	 * Create action-start module-metric
	 *
	 * @param action going to start action
	 * @param measured time when it is occurred
	 * @param context action's call parameters
	 * @return instance
	 */
	@Mapping(target = "action", expression = "java(action)" )
	@Mapping(target = "measured", source = "measured")
	@Mapping(target = "criteria", expression = "java(context.getCriteria())" )
	@Mapping(target = "input", expression = "java(context.getInput())" )
	ActionStartMetric toActionStart(ModuleAction action, Instant measured, ActionContext context);

	/**
	 * Create action-finish module-metric
	 *
	 * @param action which finish action call well
	 * @param measured time when it is occurred
	 * @param result result of action's execution
	 * @return instance
	 */
	@Mapping(target = "action", expression = "java(action)" )
	@Mapping(target = "measured", source = "measured")
	@Mapping(target = "output", source = "result")
	ActionFinishMetric toActionFinish(ModuleAction action, Instant measured, Object result);

	/**
	 * Create heart-beat values-metric
	 *
	 * @param action  action owner of the metric
	 * @param values values to check
	 * @param measured time when it is occurred
	 * @return instance
	 */
	@Mapping(target = "action", expression = "java(action)" )
	@Mapping(target = "measured", source = "measured")
	@Mapping(target = "condition", source = "values.condition")
	@Mapping(target = "active", source = "values.active")
	HeartBeatMetric toHeartBeat(String modulePK, ModuleAction action, ModuleValues values, Instant measured);

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
