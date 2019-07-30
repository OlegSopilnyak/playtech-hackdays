/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage.event;

import oleg.sopilnyak.service.configuration.storage.ConfigurationStorageRepository;

/**
 * Event to replace module's configuration items
 */
public class ReplaceConfigurationEvent extends ConfigurationStorageEvent {
	@Override
	public void update(ConfigurationStorageRepository repository) {
		repository.replaceConfiguration(module, configuration);
	}

	@Override
	public String toString() {
		return "ReplaceConfigurationEvent{" +
				"module=" + module.primaryKey() +
				'}';
	}
}
