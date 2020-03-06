/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module;

import oleg.sopilnyak.module.model.ModuleAction;

/**
 * Type - the values of the module (health & configuration) many to one relation to module
 *
 * @see oleg.sopilnyak.module.ModuleConfigurable
 * @see oleg.sopilnyak.module.ModuleHealth.Control
 * @see oleg.sopilnyak.module.ModuleHealth.State
 */
public interface ModuleValues extends ModuleHealth.Control, ModuleHealth.State, ModuleConfigurable {
	/**
	 * To get reference to main action of module
	 *
	 * @return instance
	 */
	ModuleAction getMainAction();

	/**
	 * The visitor to walk though module's values
	 */
	interface Visitor {
		void visit(ModuleValues values);
	}
}
