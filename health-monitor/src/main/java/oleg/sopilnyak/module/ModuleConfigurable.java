/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module;

import oleg.sopilnyak.module.model.VariableItem;

import java.util.Map;

/**
 * Type configurable behavior of module
 */
public interface ModuleConfigurable {
	/**
	 * To get current configuration of module
	 *
	 * @return configuration as map
	 */
	Map<String, VariableItem> getConfiguration();

	/**
	 * Notification about change configuration
	 *
	 * @param changed map with changes
	 */
	void configurationChanged(Map<String, VariableItem> changed);
}
