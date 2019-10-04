/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import oleg.sopilnyak.service.model.dto.VariableItemDto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Type to transport Remote Module's parameters to register
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RemoteModuleDto extends ModuleDto implements ModuleBasics {
	private boolean active;
	private ModuleHealthCondition condition;

	@JsonDeserialize(as = LinkedHashMap.class)
	private Map<String, VariableItemDto> configuration;
	private MetricContainerDto metrics;
}
