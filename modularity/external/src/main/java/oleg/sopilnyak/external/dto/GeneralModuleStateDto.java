/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import oleg.sopilnyak.service.model.dto.VariableItemDto;

import java.util.Map;

/**
 * Type to transport Module's general state
 */

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GeneralModuleStateDto extends ModuleStatusDto{
	// id of module main-action
	private String mainActionId;
	// updated module's configuration
	private Map<String, VariableItemDto> configuration;
}
