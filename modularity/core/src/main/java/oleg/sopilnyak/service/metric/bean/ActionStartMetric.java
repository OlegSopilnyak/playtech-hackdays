/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;

import java.util.Map;

/**
 * Type : metric for action's start
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ActionStartMetric  extends ModuleMetricAdapter {
	private Map<String, Object> criteria;
	private Object input;

	@Override
	public String getName() {
		return "start-action " + action.getName();
	}

	@Override
	protected String concreteValue() {
		return "{\"criteria\" : \""+criteria+"\", \"input\" : \""+input+"\"}";
	}
}
