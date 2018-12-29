/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.dto;

import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import org.springframework.beans.BeanUtils;

/**
 * Type: DTO type of module action
 */
public class ModuleActionDto extends ModuleActionAdapter {
	public ModuleActionDto(ModuleAction action) {
		super();
		BeanUtils.copyProperties(action, this);
	}
}
