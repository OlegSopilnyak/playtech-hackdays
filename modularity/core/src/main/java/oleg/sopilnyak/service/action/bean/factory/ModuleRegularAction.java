/*
 * Copyright (C) Oleg Sopilnyak 2019
 */

package oleg.sopilnyak.service.action.bean.factory;

import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;

import java.io.Serializable;

/**
 * Type: regular action of module
 */
public class ModuleRegularAction extends ModuleActionAdapter implements Serializable {
	public ModuleRegularAction(ModuleBasics module, String name) {
		super(module, "[" + name + "-action]");
	}
}
