/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.bean.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;

/**
 * Type: action with exception
 */
@Data
@EqualsAndHashCode(callSuper=false)
public abstract class ResultModuleAction extends ModuleActionAdapter {
	private Throwable cause;
}
