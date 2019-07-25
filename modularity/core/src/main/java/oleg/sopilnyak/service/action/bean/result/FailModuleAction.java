/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.bean.result;

import oleg.sopilnyak.module.model.ModuleAction;

/**
 * Type: action which was failed
 */
public class FailModuleAction extends ResultModuleAction {
	public FailModuleAction(ModuleAction action, Throwable cause) {
		super(action, cause);
		action.setState(ModuleAction.State.FAIL);
	}
}
