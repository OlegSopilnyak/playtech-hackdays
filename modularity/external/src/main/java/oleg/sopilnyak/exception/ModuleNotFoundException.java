/*
 * Copyright (C) Oleg Sopilnyak 2019
 */

package oleg.sopilnyak.exception;

/**
 * Exception thrown when no registered module
 */
public class ModuleNotFoundException extends RuntimeException {
	private final String modulePK;

	public ModuleNotFoundException(String modulePK) {
		super("Module not found :"+modulePK);
		this.modulePK = modulePK;
	}
}
