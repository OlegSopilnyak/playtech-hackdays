/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage;

import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.model.dto.ModuleDto;

import java.util.Map;

/**
 * Repository to do trivial operations with configuration
 */
public interface ConfigurationStorageRepository {

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
