/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage.event;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;

import java.util.Map;

/**
 * Event to add module's configuration items
 */
public class ExpandConfigurationEvent extends ConfigurationStorageEvent {
	public ExpandConfigurationEvent(Module module, Map<String, VariableItem> configuration) {
		super(module, configuration);
	}

	@Override
	public void update(ModuleConfigurationStorage.Repository repository) {
		repository.expandConfiguration(module, configuration);
	}

	@Override
	public String toString() {
		return "ExpandConfigurationEvent{" +
				"module=" + module.primaryKey() +
				'}';
	}
}
