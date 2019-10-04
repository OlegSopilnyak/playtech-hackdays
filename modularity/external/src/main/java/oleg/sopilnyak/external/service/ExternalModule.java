/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service;


import oleg.sopilnyak.external.dto.ModuleValuesDto;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleValues;

/**
 * Type - external service's module
 */
public interface ExternalModule extends Module {

	/**
	 * To get values for external host (registered previously)
	 *
	 * @param host the host where module is working
	 * @return values or null if not registered
	 */
	ModuleValues getValuesFor(String host);
	/**
	 * To check is module detached from processing
	 *
	 * @return true if external module is detached
	 */
	default boolean isDetached() {
		return !isWorking()
//				&& getCondition() == ModuleHealthCondition.DAMAGED
				;
	}

	/**
	 * The size of registered values
	 *
	 * @return the amount of registered module's values by hosts
	 */
	int valuesSize();

	/**
	 * To get host for which external module was registered
	 *
	 * @param moduleHost module's host
	 */
	void registeredFor(String moduleHost);

	/**
	 * To register module's values for further processing
	 *
	 * @param values values to register
	 * @return true if success
	 */
	boolean registerValues(ModuleValuesDto values);

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
