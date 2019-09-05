/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;

import java.util.Map;

/**
 * Type - external service's module
 */
public interface ExternalModule extends Module {
	/**
	 * To check is module detached from processing
	 *
	 * @return true if external module is detached
	 */
	default boolean detached(){
		return !isActive() && getCondition() == ModuleHealthCondition.DAMAGED;
	}

	/**
	 * To get changed module's configuration
	 *
	 * @return changed configuration
	 */
	Map<String, VariableItem> getChanged();
}
