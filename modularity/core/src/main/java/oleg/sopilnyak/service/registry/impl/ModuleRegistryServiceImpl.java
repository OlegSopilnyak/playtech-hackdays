/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.registry.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.service.ModuleServiceAdapter;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for register modules and check their registry condition
 *
 * @see oleg.sopilnyak.service.registry.ModulesRegistryService
 */
@Slf4j
public class ModuleRegistryServiceImpl extends ModuleServiceAdapter implements ModulesRegistryService {
	// the map of registered modules
	private final Map<String, Module> modules = new ConcurrentHashMap<>();
	// Primary Key of the registry module
	private final String MODULE_PK;

	public ModuleRegistryServiceImpl() {
		MODULE_PK = primaryKey();
	}

	/**
	 * To add module to registry
	 *
	 * @param module to add
	 */
	@Override
	public void register(final Module module) {
		if (module == this) {
			return;
		}
		assert module != null;
		log.debug("Registering '{}'", module.primaryKey());
		modules.putIfAbsent(module.primaryKey(), module);
	}

	/**
	 * To remove module from registry
	 *
	 * @param module to remove
	 */
	@Override
	public void remove(final Module module) {
		if (module == this) {
			return;
		}
		assert module != null;
		log.debug("Unregistering '{}'", module.primaryKey());
		modules.remove(module.primaryKey());
	}

	/**
	 * To get collection of registered modules
	 *
	 * @return collection of registered modules
	 */
	@Override
	public Collection<Module> registered() {
		final Collection<Module> registeredModules = new ArrayList<>(modules.values());
		return registeredModules.stream()
				.filter(m -> m.isModuleRegistered())
				.collect(Collectors.toCollection(LinkedList::new));
	}

	/**
	 * To get registered module by module's primary key
	 *
	 * @param modulePK primary key
	 * @return module or null if not registered
	 */
	@Override
	public Module getRegistered(String modulePK) {
		return StringUtils.isEmpty(modulePK) ? null : modules.get(modulePK);
	}

	/**
	 * To get registered module by module instance
	 *
	 * @param module module instance
	 * @return module or null if not registered
	 */
	@Override
	public Module getRegistered(ModuleBasics module) {
		assert module != null : "Module couldn't be null";
		return getRegistered(module.primaryKey());
	}

	/**
	 * Allocate module's resources and get module ready to work
	 */
	@Override
	protected void initAsService() {
		if (log.isDebugEnabled()) {
			log.debug("Initiating service...");
		}
		final Module previous = modules.putIfAbsent(MODULE_PK, this);
		if (log.isDebugEnabled()) {
			log.debug("Registered '{}' is '{}'", MODULE_PK, Objects.isNull(previous));
		}
	}

	/**
	 * Free allocated resources
	 */
	@Override
	protected void shutdownAsService() {
		modules.remove(MODULE_PK);
	}

}
