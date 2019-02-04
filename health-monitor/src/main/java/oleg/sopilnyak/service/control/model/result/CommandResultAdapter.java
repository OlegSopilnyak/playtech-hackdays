/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.model.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import oleg.sopilnyak.service.control.CommandResult;
import oleg.sopilnyak.service.control.model.ModuleCommandState;

import static oleg.sopilnyak.service.control.model.ModuleCommandState.INIT;

/**
 * Result: type-result of command's execution with state and raw data
 */
@Data
public abstract class CommandResultAdapter implements CommandResult {
	protected ModuleCommandState state;
	protected Object data;

	public CommandResultAdapter() {
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
	@Override
	public String dataAsJSON() {
		try {
			return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data);
		} catch (Throwable t) {
			return "{\"status\": \"failed :" + t.getClass().getSimpleName() + " - " + t.getMessage() + "\"}";
		}
	}

	/**
	 * To get access to external JSON mapper
	 *
	 * @return instance
	 */
	protected abstract ObjectMapper getMapper();
}
