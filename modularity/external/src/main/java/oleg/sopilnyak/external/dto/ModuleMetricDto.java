/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.model.dto.ModuleActionDto;

import java.time.Instant;

/**
 * Type: DTO type of module metrics container
 */
@Data
@EqualsAndHashCode(exclude = {"value"})
@JsonPropertyOrder({"name", "measured", "valueAsString", "action"})
public class ModuleMetricDto implements ModuleMetric {
	@JsonDeserialize(as = ModuleActionDto.class)
	private ModuleAction action;
	private Instant measured;
	private String name;
	private String valueAsString;
	@JsonIgnore
	private transient Object[] value;
}
