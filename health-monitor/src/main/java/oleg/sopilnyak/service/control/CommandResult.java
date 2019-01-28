/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control;

/**
 * Result: type-result of command's execution
 */
public interface CommandResult {

	/**
	 * To get current state of result
	 *
	 * @return current state
	 * @see State
	 */
	State getState();

	/**
	 * To get result's data
	 *
	 * @return result's data
	 */
	Object getData();

	/**
	 * To get result's data as string for console output
	 *
	 * @return data as tty string
	 */
	String dataAsTTY();

	/**
	 * To get result's data as string for JS communication
	 *
	 * @return data as json string
	 */
	String dataAsJSON();

	enum State{
		INIT, START, PROCESS, SUCCESS, FAIL
	}
}
