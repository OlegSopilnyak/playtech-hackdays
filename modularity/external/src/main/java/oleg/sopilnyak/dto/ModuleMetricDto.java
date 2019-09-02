/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.dto;

import lombok.Data;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;

import java.time.Instant;

/**
 * Type: DTO type of module metrics container
 */
@Data
public class ModuleMetricDto implements ModuleMetric {
	private ModuleAction action;
	private Instant measured;
	private String name;
	private String valueAsString;
}
