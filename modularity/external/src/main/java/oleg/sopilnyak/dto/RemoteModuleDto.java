/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import oleg.sopilnyak.service.model.dto.VariableItemDto;

import java.util.Map;

/**
 * Type to transport Remote Module's parameters to register
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RemoteModuleDto extends ModuleDto {
	private boolean active;
	private ModuleHealthCondition condition;
	private Map<String, VariableItemDto> configuration;
	private MetricContainerDto metrics;
}
