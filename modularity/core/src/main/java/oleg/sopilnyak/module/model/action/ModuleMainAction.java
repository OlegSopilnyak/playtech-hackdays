/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module.model.action;

import oleg.sopilnyak.module.ModuleBasics;

import java.io.Serializable;

/**
 * Type: main-action of module
 */
public class ModuleMainAction extends ModuleActionAdapter implements Serializable {
	public ModuleMainAction(ModuleBasics module) {
		super(module, "[main-module-action]");
	}
}
