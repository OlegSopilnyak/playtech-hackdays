/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage.event;

import lombok.Data;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.ConfigurationStorageRepository;
import oleg.sopilnyak.service.model.dto.ModuleDto;

import java.io.Serializable;
import java.util.Map;

/**
 * The parent of any type of storage events
 */
@Data
public abstract class ConfigurationStorageEvent implements Serializable {
	private static final long serialVersionUID = -3402253351758269847L;
	protected ModuleDto module;
	protected Map<String, VariableItem> configuration;

	/**
	 * To do activity in the repository
	 *
	 * @param repository way to database
	 */
	public abstract void update(ConfigurationStorageRepository repository);
}
