/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.action;

import oleg.sopilnyak.module.model.ModuleAction;
import org.springframework.beans.BeanUtils;

/**
 * Type: action which is finished successfully
 */
public class SuccessModuleAction extends ModuleActionAdapter {
	public SuccessModuleAction(ModuleAction action) {
		super();
		BeanUtils.copyProperties(action, this);
	}
}
