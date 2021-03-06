/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric;

import lombok.Data;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.model.DtoMapper;
import oleg.sopilnyak.service.model.dto.ModuleActionDto;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Objects;

/**
 * Type: metric basic features
 */
@Data
public abstract class ModuleMetricAdapter implements ModuleMetric {
	// action-owner of metric
	protected ModuleAction action;
	// when metric was measured
	protected Instant measured;

	/**
	 * To compose string from array of values according to metric's type
	 *
	 * @return representation of value as string
	 */
	@Override
	public String valuesAsString() {
		final String concreteValue = concreteValue();
		final ModuleActionDto action = DtoMapper.INSTANCE.toActionDto(getAction());

		final StringBuilder valueBuilder = new StringBuilder("Metric ")
				.append(getName())
				.append(" [").append(getMeasured()).append("]");
		// save extra value
		if (!StringUtils.isEmpty(concreteValue)) {
			valueBuilder.append(" value: ").append(concreteValue);
		}
		// save action's info
		valueBuilder.append(" | in action ").append(action.valueAsString());
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
		return "";
	}

	@Override
	public String toString() {
		return "Metric<" + getClass().getSimpleName() + ">{" +
				"action=" + action +
				", measured=" + measured +
				'}';
	}

}
