/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.commands.impl;

import oleg.sopilnyak.commands.ModuleCommand;
import oleg.sopilnyak.commands.ModuleCommandFactory;
import oleg.sopilnyak.commands.model.ModuleCommandType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Factory: of modules control commands
 */
public class ModuleCommandFactoryImpl implements ModuleCommandFactory {
	private final Map<ModuleCommandType, Class<? extends ModuleCommand>> commandsStore;

	public ModuleCommandFactoryImpl(Map<ModuleCommandType, Class<? extends ModuleCommand>> commandsStore) {
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
	public ModuleCommand create(ModuleCommandType type) {
		final Class<? extends ModuleCommand> clazz = commandsStore.get(type);
		return Objects.isNull(clazz) ? null : spring.getBean(clazz);
	}

	/**
	 * To get collection of available commands
	 *
	 * @return commands set of the factory
	 */
	@Override
	public Collection<String> availableCommands() {
		return commandsStore.keySet().stream().map(type->type.name().toLowerCase()).collect(Collectors.toList());
	}
}
