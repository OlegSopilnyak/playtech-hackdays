/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action;

import oleg.sopilnyak.module.model.ModuleAction;

/**
 * Repository: storing module's actions
 */
public interface ModuleActionsRepository {
	/**
	 * To store action to repository
	 *
	 * @param action item to store
	 */
	void persist(ModuleAction action);

	/**
	 * To get ModuleAction by Id
	 *
	 * @param actionId id of action
	 * @return action instance or null if not exists
	 */
	ModuleAction getById(String actionId);
}
