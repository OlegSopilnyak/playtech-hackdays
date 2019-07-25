/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.factory;

import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.service.action.ModuleActionAdapter;

import java.io.Serializable;

/**
 * Type: main-action of module
 */
public class ModuleMainAction extends ModuleActionAdapter implements Serializable {
	public ModuleMainAction(ModuleBasics module) {
		super(module, "[main-module-action]");
	}
}
