/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.bean.result;

import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;
import org.springframework.beans.BeanUtils;

public abstract class ResultModuleAction extends ModuleActionAdapter {
	private final Throwable cause;

	public ResultModuleAction(ModuleAction context, Throwable cause) {
		this.cause = cause;
		BeanUtils.copyProperties(context, this);
	}
	public ResultModuleAction(ModuleAction context) {
		this.cause = null;
		BeanUtils.copyProperties(context, this);
	}

	public Throwable getCause() {
		return cause;
	}
}
