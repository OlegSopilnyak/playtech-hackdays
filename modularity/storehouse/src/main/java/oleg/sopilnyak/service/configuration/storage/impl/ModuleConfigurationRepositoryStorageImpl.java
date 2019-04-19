/*
 * Copyright (C) Oleg Sopilnyak 2019
 */

package oleg.sopilnyak.service.configuration.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service, realization through data-repository
 */
public class ModuleConfigurationRepositoryStorageImpl extends ModuleConfigurationStorageAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(ModuleConfigurationRepositoryStorageImpl.class);

	@Autowired
	private Repository repository;
	/**
	 * To get access to logger of storage's realization
	 *
	 * @return logger
	 */
	@Override
	protected Logger getLogger() {
		return LOG;
	}

	/**
	 * To get access to configurations store
	 *
	 * @return instance
	 */
	@Override
	public Repository getConfigurationRepository() {
		return repository;
	}
}
