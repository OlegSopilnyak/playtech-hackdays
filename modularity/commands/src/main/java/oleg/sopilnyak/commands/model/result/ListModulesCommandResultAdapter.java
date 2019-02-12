/*
  Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.commands.model.result;

import oleg.sopilnyak.commands.model.ModuleInfoAdapter;

import java.util.List;
import java.util.Objects;

/**
 * Result: type-result of command's execution with state and data as list
 */
public abstract class ListModulesCommandResultAdapter extends CommandResultAdapter {
	/**
	 * To get result's data as string for console output
	 *
	 * @return data as tty string
	 */
	@Override
	public String dataAsTTY() {
		final List<ModuleInfoAdapter> modules = (List<ModuleInfoAdapter>) data;
		StringBuilder builder = new StringBuilder("Modules selected: ")
				.append(modules == null ? 0 : modules.size())
				.append("\n").append("-------------\n");
		if (Objects.nonNull(modules)) {
			modules.forEach(info -> builder.append(info.toTTY()).append("\n"));
		}
		return builder.toString();
	}
}
