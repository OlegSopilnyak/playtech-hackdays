/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import oleg.sopilnyak.module.model.ModuleHealthCondition;

import java.io.Serializable;

/**
 * Type: brief information about module
 */
@Data
@AllArgsConstructor
public class ModuleInfo implements Serializable {
	protected String modulePK;
	protected boolean active;
	protected ModuleHealthCondition condition;
	protected String description;
	public String toTTY(){
		return "";
	}
}
