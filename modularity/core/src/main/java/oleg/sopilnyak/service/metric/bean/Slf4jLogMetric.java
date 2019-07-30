/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;

/**
 * Type : metric for logger-module
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class Slf4jLogMetric  extends ModuleMetricAdapter {
	private String message;
	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return "logger";
	}

	/**
	 * To fill values metric's depended information
	 *
	 * @return concrete
	 */
	@Override
	protected String concreteValue() {
		return message;
	}

	@Override
	public String toString() {
		return "LoggerMetric{" +
				"action='" + super.getAction().getName() + '\'' +
				" time='" + super.getMeasured() + '\'' +
				" message='" + message + '\'' +
				'}';
	}
}

