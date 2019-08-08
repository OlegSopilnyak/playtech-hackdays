/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import oleg.sopilnyak.module.model.ModuleHealthCondition;

/**
 * Type to transport Module's status
 */
@Data
@EqualsAndHashCode
@ToString
public class ModuleStatusDto {
	private String modulePK;
	private String description;
	private boolean active;
	private ModuleHealthCondition condition;

}
