/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Type: metric for fail action
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ActionExceptionMetric extends ActionChangedMetric {
	private Throwable cause;

	@Override
	public String getName() {
		return "exception in '" + this.getAction().getName() + "'";
	}

	@Override
	public Object[] getValue() {
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
