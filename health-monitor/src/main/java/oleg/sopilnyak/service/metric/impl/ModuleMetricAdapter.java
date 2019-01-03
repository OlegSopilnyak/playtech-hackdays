/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import lombok.Data;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.dto.ModuleActionDto;

import java.time.Instant;

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
		final String value = "Metric " + name() + "[" + measured + "] value: " + concreteValue() + " | in action " + action.valueAsString();
		if (action.getParent() != null) {
			return value + ", parent id:" + action.getParent().getId();
		}
		return value;
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
