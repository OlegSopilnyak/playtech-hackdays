/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;

/**
 * Type: DTO type of module action
 */
public class ModuleActionDto extends ModuleActionAdapter {
	@JsonDeserialize(as = ModuleDto.class)
	@Override
	public ModuleBasics getModule() {
		return super.getModule();
	}
}
