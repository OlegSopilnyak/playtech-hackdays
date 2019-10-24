/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service;

import oleg.sopilnyak.external.service.impl.ExternalModuleImpl;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;

/**
 * The factory of distributed external modules
 *
 * @see ExternalModule
 */
public interface DistributedExternalModulesFactory {
	/**
	 * To update external module in factory
	 *
	 * @param module to be updated
	 */
	void updateModule(ExternalModuleImpl module);

	/**
	 * To retrieve external module from factory by primaryKey
	 *
	 * @param modulePK primary key of module to retrieve
	 * @return retrieved module or null if not exists
	 * @see Module#primaryKey()
	 */
	ExternalModuleImpl retrieveModule(String modulePK);

	/**
	 * To remove external module from factory
	 *
	 * @param module to be removed
	 */
	void removeModule(ExternalModuleImpl module);

	/**
	 * To retrieve or create external module by pattern
	 *
	 * @param pattern the pattern for external module's creation
	 * @return exists or new instance of external module
	 */
	ExternalModuleImpl retrieveModuleBy(ModuleBasics pattern);
}
