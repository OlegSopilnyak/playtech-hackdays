/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.impl;

import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;

import java.io.Serializable;

/**
 * Type: main action of module
 */

class ModuleMainAction extends ModuleActionAdapter implements Serializable {
	public ModuleMainAction(ModuleBasics module) {
		super(module, "[main-module-action]");
	}
}
