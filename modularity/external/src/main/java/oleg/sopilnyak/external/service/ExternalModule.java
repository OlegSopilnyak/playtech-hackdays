/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service;


import oleg.sopilnyak.external.dto.ModuleValuesDto;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleValues;

import java.io.Serializable;

/**
 * Type - external service's module
 */
public interface ExternalModule extends Module, Serializable {
	/**
	 * To check is module didn't touched during expired duration
	 *
	 * @param moduleExpiredDuration expired duration
	 * @return true if module didn't touched during expired duration
	 */
	boolean isExpired(long moduleExpiredDuration);

	/**
	 * The check is module has set of values
	 *
	 * @return returns true if has
	 */
	boolean hasValues();

	/**
	 * To get values for external host (registered previously)
	 *
	 * @param host the host where module is working
	 * @return values or null if not registered
	 */
	ModuleValues valuesFor(String host);

	/**
	 * To get host where external module is registered for synchronization
	 *
	 * @param registryHost registry's host
	 */
	void registryIn(String registryHost);

	/**
	 * To get the host where external module is registered for synchronization
	 *
	 * @return registry host
	 */
	String registryIn();

	/**
	 * To register module's values for further processing
	 *
	 * @param values values to register
	 * @return true if success
	 */
	boolean registerValues(ModuleValuesDto values);

	/**
	 * To un-register values from the module
	 *
	 * @param values registered values
	 * @return true if success
	 */
	boolean unRegisterValues(ModuleValues values);


	/**
	 * To get changed module's configuration
	 *
	 * @return changed configuration
	 */
//	Map<String, VariableItemDto> getChanged();

	/**
	 * To setup main module action
	 *
	 * @param moduleMainAction stored main module action
	 */
//	void setMainAction(ModuleAction moduleMainAction);

	/**
	 * To merge main configuration with changed
	 */
//	void repairConfiguration();
}
