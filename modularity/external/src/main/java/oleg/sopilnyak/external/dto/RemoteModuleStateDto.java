/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import oleg.sopilnyak.module.model.ModuleHealthCondition;

/**
 * Type to transport Module's current state
 */

@Data
@EqualsAndHashCode
@ToString
public class RemoteModuleStateDto {
	// primary key of module
	private String modulePK;
	// flag is module active
	private boolean active;
	// health condition of module
	private ModuleHealthCondition condition;
	// metrics of remote module
	private MetricContainerDto metrics;
}
