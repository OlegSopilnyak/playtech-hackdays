/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module.metric.storage;

import java.time.Instant;

/**
 * Type: criteria for metrics selections
 */
public interface SelectCriteria {
	String getHost();
	String getName();
	String getModule();
	Instant getFrom();
	Instant getTo();
	String getValueReg();
}
