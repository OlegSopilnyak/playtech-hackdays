/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import oleg.sopilnyak.service.control.ModuleCommand;
import oleg.sopilnyak.service.control.ModuleCommandFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Objects;

/**
 * Factory: of modules control commands
 */
public class ModuleCommandFactoryImpl implements ModuleCommandFactory {
	private final Map<ModuleCommand.Type, Class<? extends ModuleCommand>> commandsStore;

	public ModuleCommandFactoryImpl(Map<ModuleCommand.Type, Class<? extends ModuleCommand>> commandsStore) {
		this.commandsStore = commandsStore;
	}

	@Autowired
	private ApplicationContext spring;

	/**
	 * To create new command of appropriate type
	 *
	 * @param type type of command
	 * @return instance
	 */
	@Override
	public ModuleCommand create(ModuleCommand.Type type) {
		final Class<? extends ModuleCommand> clazz = commandsStore.get(type);
		return Objects.isNull(clazz) ? null : spring.getBean(clazz);
	}
}
