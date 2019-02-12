/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.impl;

import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;

/**
 * Type: regular action of module
 */
class ModuleRegularAction extends ModuleActionAdapter {
	public ModuleRegularAction(ModuleBasics module, String name) {
		super(module, "[" + name + "]");
	}
}
