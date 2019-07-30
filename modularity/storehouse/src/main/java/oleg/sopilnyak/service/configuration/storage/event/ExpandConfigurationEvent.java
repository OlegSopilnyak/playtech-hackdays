/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage.event;

import oleg.sopilnyak.service.configuration.storage.ConfigurationStorageRepository;

/**
 * Event to add module's configuration items
 */
public class ExpandConfigurationEvent extends ConfigurationStorageEvent {
	@Override
	public void update(ConfigurationStorageRepository repository) {
		repository.expandConfiguration(module, configuration);
	}

	@Override
	public String toString() {
		return "ExpandConfigurationEvent{" +
				"module=" + module.primaryKey() +
				'}';
	}
}
