/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.storage;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;

/**
 * Service: storage of module's actions
 */
public interface ModuleActionStorage {

	/**
	 * To create and save main-action for module
	 *
	 * @param module owner of action
	 * @return new instance
	 */
	ModuleAction createActionFor(Module module);

	/**
	 * To create and save regular action for the module
	 *
	 * @param module owner of action
	 * @param parent  action parent of new action
	 * @param name the name of action
	 * @return new instance
	 */
	ModuleAction createActionFor(Module module, ModuleAction parent, String name);

	/**
	 * To persist Module's action
	 *
	 * @param action action to store
	 */
	void persist(ModuleAction action);

	/**
	 * To get stored action by ID
	 *
	 * @param actionId id of action
	 * @return instance or null if not exists
	 */
	ModuleAction getById(String actionId);
}
