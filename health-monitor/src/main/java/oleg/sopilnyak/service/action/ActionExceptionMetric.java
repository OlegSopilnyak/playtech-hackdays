/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action;

import oleg.sopilnyak.module.model.ModuleAction;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

/**
 * Type: metric for fail action
 */
public class ActionExceptionMetric extends ActionChangedMetric {
	private final Throwable cause;
	public ActionExceptionMetric(ModuleAction action, Instant now, Throwable cause) {
		super(action, now);
		this.cause = cause;
	}

	/**
	 * To get the name of metric
	 *
	 * @return the name
	 */
	@Override
	public String name() {
		return "exception";
	}

	/**
	 * The values of metric, according to metric's type it maybe one or several values
	 *
	 * @return array of values
	 */
	@Override
	public Object[] value() {
		return new Object[]{cause};
	}

	/**
	 * To fill values metric's depended information
	 *
	 * @return concrete
	 */
	@Override
	protected String concreteValue() {
		if (cause != null){
			final StringWriter message = new StringWriter();
			try(PrintWriter out = new PrintWriter(message, true)) {
				cause.printStackTrace(out);
			}
			return message.toString();
		} else {
			return super.concreteValue();
		}
	}
}
