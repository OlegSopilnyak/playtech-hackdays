/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service.impl;

import oleg.sopilnyak.external.service.ExternalModule;

/**
 * Service to check is module valid
 */
interface ExternalModuleChecker {
	/**
	 * To check is external-module registered well
	 *
	 * @param module module to check
	 * @return true if registered well
	 */
	boolean isValidModule(ExternalModule module);
}
