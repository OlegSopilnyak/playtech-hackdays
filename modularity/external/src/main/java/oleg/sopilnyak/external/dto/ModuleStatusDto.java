/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import oleg.sopilnyak.module.model.ModuleHealthCondition;

import java.io.Serializable;

/**
 * Type to transport Module's status
 */
@Data
@EqualsAndHashCode
@ToString
public class ModuleStatusDto implements Serializable {
	// primary key of module
	private String modulePK;
	// description of module
	private String description;
	// flag is module active
	private boolean active;
	// health condition of module
	private ModuleHealthCondition condition;
}
