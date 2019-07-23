/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage.impl;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.ConfigurationStorageEvent;
import oleg.sopilnyak.service.configuration.storage.ConfigurationStorageRepository;

import java.util.Map;

/**
 * Event to replace module's configuration items
 */
class ReplaceConfigurationEvent extends ConfigurationStorageEvent {
	public ReplaceConfigurationEvent(Module module, Map<String, VariableItem> configuration) {
		super(module, configuration);
	}

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
