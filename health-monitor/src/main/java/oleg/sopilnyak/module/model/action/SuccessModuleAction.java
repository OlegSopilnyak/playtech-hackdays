/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.action;

import oleg.sopilnyak.module.model.ModuleAction;

/**
 * Type: action which is finished successfully
 */
public class SuccessModuleAction extends ResultModuleAction {
	public SuccessModuleAction(ModuleAction action) {
		super(action);
	}
}
