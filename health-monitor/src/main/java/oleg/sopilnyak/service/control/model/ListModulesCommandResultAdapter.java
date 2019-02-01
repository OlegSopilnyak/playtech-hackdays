/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.model;

import java.util.List;
import java.util.Objects;

/**
 * Result: type-result of command's execution with state and data as list
 */
public class ListModulesCommandResultAdapter extends AbstractCommandResult {
	/**
	 * To get result's data as string for console output
	 *
	 * @return data as tty string
	 */
	@Override
	public String dataAsTTY() {
		final List<ModuleInfo> modules = (List<ModuleInfo>) data;
		StringBuilder builder = new StringBuilder("Modules selected: ")
				.append(modules == null ? 0 : modules.size())
				.append("\n").append("-------------\n");
		if (Objects.nonNull(modules)) {
			modules.forEach(info -> builder.append(info.toTTY()).append("\n"));
		}
		return builder.toString();
	}

	/**
	 * To get result's data as string for JS communication
	 *
	 * @return data as json string
	 */
	@Override
	public String dataAsJSON() {
		return null;
	}
}
