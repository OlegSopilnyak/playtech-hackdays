/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.model.dto;

import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.action.ModuleActionAdapter;
import org.springframework.beans.BeanUtils;

/**
 * Type: DTO type of module action
 */
public class ModuleActionDto extends ModuleActionAdapter {
	public ModuleActionDto(ModuleAction action) {
		super(action.getModule(), action.getParent(), action.getName());
		BeanUtils.copyProperties(action, this);
	}
}
