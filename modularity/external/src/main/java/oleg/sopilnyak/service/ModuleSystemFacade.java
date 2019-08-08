/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service;

import oleg.sopilnyak.dto.ModuleStatusDto;

import java.util.List;

/**
 * Facade: working with remote modules
 */
public interface ModuleSystemFacade {
	/**
	 * To get list of registered modules
	 *
	 * @return list of registered modules
	 */
	List<String> registeredModules();

	/**
	 * To get the status of particular module
	 *
	 * @param modulePK Primary Key of module
	 * @return module's status
	 */
	ModuleStatusDto moduleStatus(String modulePK);
	/**
	 * Try to start particular module
	 *
	 * @param modulePK Primary Key of module
	 * @return module's status
	 */
	ModuleStatusDto moduleStart(String modulePK);
	/**
	 * Try to stop particular module
	 *
	 * @param modulePK Primary Key of module
	 * @return module's status
	 */
	ModuleStatusDto moduleStop(String modulePK);
}
