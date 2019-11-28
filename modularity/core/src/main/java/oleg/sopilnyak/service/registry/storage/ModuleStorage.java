/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.registry.storage;

import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.ModuleHealth;

/**
 * Storage for all registered modules' health-state
 */
public interface ModuleStorage {
	/**
	 * To save current health state of service module
	 *
	 * @param module owner of state
	 * @param state  state's value
	 */
	void saveHealthState(ModuleBasics module, ModuleHealth.State state);

	/**
	 * To remove module's health state after unregister
	 *
	 * @param module owner of state
	 * @param state  state's value
	 */
	void removeHealthState(ModuleBasics module, ModuleHealth.State state);
}
