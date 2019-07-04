/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.impl;

import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.service.model.dto.ModuleDto;

/**
 * Type: action-wrapper for action to store
 */
class ActionStorageWrapper extends ModuleActionAdapter {
	public ActionStorageWrapper(ModuleAction action) {
		this.id = action.getId();
		this.module = new ModuleDto(action.getModule());
		this.parent = action.getParent();
		this.name = action.getName();
		this.description = action.getDescription();
		this.started = action.getStarted();
		this.duration = action.getDuration();
		this.hostName = action.getHostName();
		this.state  = action.getState();
	}
}
