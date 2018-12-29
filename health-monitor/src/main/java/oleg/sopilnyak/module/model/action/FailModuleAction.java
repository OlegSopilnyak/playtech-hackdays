/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.action;

import oleg.sopilnyak.module.model.ModuleAction;
import org.springframework.beans.BeanUtils;

/**
 * Type: action which was failed
 */
public class FailModuleAction extends ModuleActionAdapter {
	private final Throwable cause;
	public FailModuleAction(ModuleAction action, Throwable cause) {
		super();
		this.cause = cause;
		BeanUtils.copyProperties(action, this);
	}

	public Throwable getCause() {
		return cause;
	}
}
