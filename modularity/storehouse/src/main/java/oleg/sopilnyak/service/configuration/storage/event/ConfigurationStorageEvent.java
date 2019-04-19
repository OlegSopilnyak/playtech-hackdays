/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage.event;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.dto.ModuleDto;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The parent of any type of storage events
 */
public abstract class ConfigurationStorageEvent implements Serializable {
	private static final long serialVersionUID = -3402253351758269847L;
	protected final ModuleDto module;
	protected final Map<String, VariableItem> configuration;

	public ConfigurationStorageEvent(Module module, Map<String, VariableItem> configuration) {
		this.module = new ModuleDto(module);
		this.configuration = new LinkedHashMap<>(configuration);
	}

	public abstract void update(ModuleConfigurationStorage.Repository store);
}
