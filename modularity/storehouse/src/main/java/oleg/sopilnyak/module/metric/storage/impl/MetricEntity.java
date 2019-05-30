/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module.metric.storage.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Type: bean for store metric
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class MetricEntity {
	private String name;
	private String module;
	private Instant measured;
	private String actionId;
	private String host;
	private String valueAsString;
}
