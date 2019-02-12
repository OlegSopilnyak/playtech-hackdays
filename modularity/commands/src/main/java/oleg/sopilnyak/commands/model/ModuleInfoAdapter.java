/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.commands.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import oleg.sopilnyak.module.model.ModuleHealthCondition;

import java.io.Serializable;

/**
 * Type: brief information about module
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(doNotUseGetters = true)
public abstract class ModuleInfoAdapter implements Serializable {
	protected String modulePK;
	protected boolean active;
	protected ModuleHealthCondition condition;
	protected String description;
	public String toTTY(){
		return "";
	}
}
