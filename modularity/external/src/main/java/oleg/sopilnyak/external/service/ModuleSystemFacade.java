/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service;

import oleg.sopilnyak.external.dto.ExternalModuleStateDto;
import oleg.sopilnyak.external.dto.GeneralModuleStateDto;
import oleg.sopilnyak.external.dto.ModuleStatusDto;
import oleg.sopilnyak.external.dto.RemoteModuleDto;
import oleg.sopilnyak.service.model.dto.ModuleDto;

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

	/**
	 * To register external module
	 *
	 * @param remoteModule remote module
	 * @param moduleHost owner of remote module
	 * @return status of registered module
	 */
	ModuleStatusDto registerModule(RemoteModuleDto remoteModule, String moduleHost);

	/**
	 * To un-register external module
	 *
	 * @param remoteModule remote module
	 * @param moduleHost owner of remote module
	 * @return last status of module
	 */
	ModuleStatusDto unRegisterModule(ModuleDto remoteModule, String moduleHost);

	/**
	 * To update status of external module
	 *
	 * @param externalState remote state of external module
	 * @param moduleHost owner of remote module
	 * @return updated state of external module (include module configuration updates)
	 */
	GeneralModuleStateDto status(ExternalModuleStateDto externalState, String moduleHost);
}
