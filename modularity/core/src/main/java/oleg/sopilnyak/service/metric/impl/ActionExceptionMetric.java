/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.metric.impl;

import oleg.sopilnyak.module.model.ModuleAction;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

/**
 * Type: metric for fail action
 */
class ActionExceptionMetric extends ActionChangedMetric {
	private final Throwable cause;

	ActionExceptionMetric(ModuleAction action, Instant now, Throwable cause) {
		super(action, now);
		this.cause = cause;
	}

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
