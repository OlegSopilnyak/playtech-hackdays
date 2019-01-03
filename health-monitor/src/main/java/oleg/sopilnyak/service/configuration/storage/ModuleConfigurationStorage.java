/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.VariableItem;

import java.util.Collection;
import java.util.Map;

/**
 * Service: active storage of modules' configurations
 */
public interface ModuleConfigurationStorage {
	/**
	 * To get updated configured variables
	 *
	 * @param module               the consumer of configuration
	 * @param currentConfiguration current state of configuration
	 * @return updated variables (emptyMap if none)
	 */
	Map<String, VariableItem> getUpdatedVariables(Module module, Map<String, VariableItem> currentConfiguration);

	/**
	 * To add modules configuration change listener
	 *
	 * @param listener listener of changes
	 */
	void addConfigurationListener(ConfigurationListener listener);
	/**
	 * To remove modules configuration change listener
	 *
	 * @param listener listener of changes
	 */
	void removeConfigurationListener(ConfigurationListener listener);
	// inner classes

	/**
	 * Listener of configurations changes
	 */
	interface ConfigurationListener {
		/**
		 * Notification about change configuration for modules
		 *
		 * @param modules modules which configuration was changed
		 */
		void changedModules(Collection<String> modules);
	}
}
