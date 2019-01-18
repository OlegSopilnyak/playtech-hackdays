/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import lombok.Data;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.dto.ModuleActionDto;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Objects;

/**
 * Type: metric basic features
 */
@Data
public abstract class ModuleMetricAdapter implements ModuleMetric {
	private final ModuleActionDto action;
	private final Instant measured;

	public ModuleMetricAdapter(ModuleAction action, Instant measured) {
		this.action = new ModuleActionDto(action);
		this.measured = measured;
	}

	@Override
	public String toString() {
		return "Metric<" + getClass().getSimpleName() + ">{" +
				"action=" + action +
				", measured=" + measured +
				'}';
	}

	/**
	 * To get action-owner of metrics
	 *
	 * @return reference to action
	 */
	@Override
	public ModuleAction action() {
		return action;
	}

	/**
	 * To get time when metric was took
	 *
	 * @return the time
	 */
	@Override
	public Instant measured() {
		return measured;
	}

	/**
	 * To compose string from array of values according to metric's type
	 *
	 * @return representation of value as string
	 */
	@Override
	public String valuesAsString() {
		final String concreteValue = concreteValue();
		final StringBuilder valueBuilder = new StringBuilder("Metric ")
				.append(name())
				.append("[").append(measured).append("]")
				.append(StringUtils.isEmpty(concreteValue) ? "" : " value: ")
				.append(StringUtils.isEmpty(concreteValue) ? "" : concreteValue)
				.append(" | in action ").append(action.valueAsString());
		if (Objects.nonNull(action.getParent())) {
			valueBuilder.append(", parent id: ").append(action.getParent().getId());
		}
		return valueBuilder.toString();
	}

	/**
	 * To fill values metric's depended information
	 *
	 * @return concrete
	 */
	protected String concreteValue() {
		return "none";
	}
}
