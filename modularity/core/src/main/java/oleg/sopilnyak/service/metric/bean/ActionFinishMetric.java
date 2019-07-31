/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;

/**
 * Type : metric for action's finish
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ActionFinishMetric extends ModuleMetricAdapter {
	private Object output;

	@Override
	public String getName() {
		return "finish-action " + action.getName();
	}

	@Override
	protected String concreteValue() {
		return "{\"output\" : \""+ output +"\"}";
	}
}
