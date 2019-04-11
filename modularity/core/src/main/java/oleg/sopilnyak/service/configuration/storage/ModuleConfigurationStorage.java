/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.dto.ModuleDto;

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
	 * To update configuration of module
	 *
	 * @param module target module
	 * @param configuration new configuration
	 */
	void updateConfiguration(Module module, Map<String, VariableItem> configuration);

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
	/**
	 * To get access to configurations store
	 *
	 * @return instance
	 */
	Repository getConfigurationRepository();

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
	/**
	 * Service, the store of modules' configurations
	 */
	interface Repository {

		/**
		 * To add items to exists configuration
		 *
		 * @param module module-owner of configuration
		 * @param configuration configuration's items to add
		 */
		void expandConfiguration(ModuleDto module, Map<String, VariableItem> configuration);

		/**
		 * To replace items of exists module configuration
		 *
		 * @param module module-owner of configuration
		 * @param configuration configuration's items to replace
		 */
		void replaceConfiguration(ModuleDto module, Map<String, VariableItem> configuration);

		/**
		 * To get configuration of module
		 *
		 * @param module the owner of configuration
		 * @return stored module's configuration
		 */
		Map<String, VariableItem> getConfiguration(ModuleDto module);
	}
}
