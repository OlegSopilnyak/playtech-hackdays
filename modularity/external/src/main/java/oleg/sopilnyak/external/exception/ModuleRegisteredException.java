/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.exception;

/**
 * Exception thrown when the module registered already
 */
public class ModuleRegisteredException extends RuntimeException {
	private final String modulePK;

	public ModuleRegisteredException(String modulePK) {
		super("Module registered already :" + modulePK);
		this.modulePK = modulePK;
	}
}
