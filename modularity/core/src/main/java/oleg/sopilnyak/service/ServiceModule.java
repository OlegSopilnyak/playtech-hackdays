/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleValues;

/**
 * Type: base for service's modules
 */
public interface ServiceModule extends Module, ModuleValues {
	/**
	 * To refresh module's state before return from registry
	 *
	 * @return true if registered
	 */
	@Override
	default boolean isModuleRegistered() {
		return true;
	}
}
