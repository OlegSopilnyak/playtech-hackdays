/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.dto;

import lombok.Data;
import oleg.sopilnyak.module.ModuleValues;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Type: transport type for module's values
 */
@Data
public class ModuleValuesDto implements ModuleValues {
	private String host;
	private boolean active;
	private ModuleAction mainAction;
	private ModuleHealthCondition condition;
	private Map<String, VariableItem> configuration;
	private Map<String, VariableItem> changed;

	/**
	 * Notification about change configuration
	 *
	 * @param changed map with changes
	 */
	@Override
	public void configurationChanged(Map<String, VariableItem> changed) {
		this.changed = changed;
	}

	public void repairConfiguration() {
		configuration = new LinkedHashMap<>(configuration);
		configuration.putAll(changed);
		changed = Collections.EMPTY_MAP;
	}

	/**
	 * After action detected fail
	 *
	 * @param exception cause of fail
	 */
	@Override
	public void healthGoDown(Throwable exception) {
		throw new UnsupportedOperationException("Not supported for external module.");
	}

	/**
	 * To get instance of last thrown exception
	 *
	 * @return exception or null if wouldn't
	 */
	@Override
	public Throwable lastThrown() {
		throw new UnsupportedOperationException("Not supported for external module.");
	}

	/**
	 * After action detected success
	 */
	@Override
	public void healthGoUp() {
		throw new UnsupportedOperationException("Not supported for external module.");
	}
}
