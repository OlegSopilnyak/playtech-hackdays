/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.action.exception;

import oleg.sopilnyak.module.model.ModuleAction;

/**
 * Exception: Wrapper to rethrow exception during action's execution
 */
public class ModuleActionRuntimeException extends RuntimeException{
	private final ModuleAction action;
	/**
	 * Constructs a new runtime exception with the specified detail message and
	 * cause.  <p>Note that the detail message associated with
	 * {@code cause} is <i>not</i> automatically incorporated in
	 * this runtime exception's detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval
	 *                by the {@link #getMessage()} method).
	 * @param cause   the cause (which is saved for later retrieval by the
	 *                {@link #getCause()} method).  (A <tt>null</tt> value is
	 *                permitted, and indicates that the cause is nonexistent or
	 *                unknown.)
	 * @since 1.4
	 */
	public ModuleActionRuntimeException(ModuleAction action, String message, Throwable cause) {
		super(message, cause);
		this.action = action;
	}

	public ModuleAction getAction() {
		return action;
	}
}
