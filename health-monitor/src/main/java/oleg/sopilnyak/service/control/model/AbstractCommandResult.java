/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.model;

import lombok.Data;
import oleg.sopilnyak.service.control.CommandResult;

import static oleg.sopilnyak.service.control.model.ModuleCommandState.INIT;

@Data
public abstract class AbstractCommandResult implements CommandResult {
	protected ModuleCommandState state;
	protected Object data;

	public AbstractCommandResult() {
		state = INIT;
	}

	/**
	 * To get result's data as string for console output
	 *
	 * @return data as tty string
	 */
	public abstract String dataAsTTY();

	/**
	 * To get result's data as string for JS communication
	 *
	 * @return data as json string
	 */
	public abstract String dataAsJSON();
}
