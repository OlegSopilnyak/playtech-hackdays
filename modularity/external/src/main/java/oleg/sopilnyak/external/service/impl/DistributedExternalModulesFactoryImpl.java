/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.external.dto.MetricContainerDto;
import oleg.sopilnyak.external.service.DistributedExternalModulesFactory;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Realization of distributed external modules factory
 */
@Slf4j
public class DistributedExternalModulesFactoryImpl implements DistributedExternalModulesFactory {
	// distributed map of registered external modules
	@Autowired
	@Qualifier("registered-modules-map")
	Map<String, ExternalModuleImpl> sharedRegisteredModulesMap;

	/**
	 * To get stream of registered external modules
	 *
	 * @return stream of modulePKs
	 */
	@Override
	public Stream<String> registeredModules() {
		return sharedRegisteredModulesMap.keySet().stream();
	}

	/**
	 * To update external module in factory
	 *
	 * @param module to be updated
	 */
	@Override
	public void updateModule(ExternalModuleImpl module) {
		final String modulePK = module.primaryKey();
		log.debug("Updating module '{}'", modulePK);
		sharedRegisteredModulesMap.put(modulePK, module);
	}

	/**
	 * To retrieve external module from factory by primaryKey
	 *
	 * @param modulePK primary key of module to retrieve
	 * @return retrieved module or null if not exists
	 * @see Module#primaryKey()
	 */
	@Override
	public ExternalModuleImpl retrieveModule(String modulePK) {
		log.debug("Retrieving module '{}'", modulePK);
		return sharedRegisteredModulesMap.get(modulePK);
	}

	/**
	 * To remove external module from factory
	 *
	 * @param module to be removed
	 */
	@Override
	public void removeModule(ExternalModuleImpl module) {
		final String modulePK = module.primaryKey();
		log.debug("Removing module '{}'", modulePK);
		sharedRegisteredModulesMap.remove(modulePK);
	}

	/**
	 * To retrieve or create external module by pattern
	 *
	 * @param pattern the pattern for external module's creation
	 * @return exists or new instance of external module
	 */
	@Override
	public ExternalModuleImpl retrieveModuleBy(ModuleBasics pattern) {
		final String modulePK = pattern.primaryKey();
		log.debug("Get or Create module '{}'", modulePK);
		return sharedRegisteredModulesMap.computeIfAbsent(modulePK, (pk) -> createExternalModule(pattern));
	}

	// private methods
	ExternalModuleImpl createExternalModule(ModuleBasics pattern) {
		final ExternalModuleImpl module = new ExternalModuleImpl();
		module.setSystemId(pattern.getSystemId());
		module.setModuleId(pattern.getModuleId());
		module.setVersionId(pattern.getVersionId());
		module.setDescription(pattern.getDescription());
		module.setModuleValues(new LinkedHashMap<>());
		module.setMetricsContainer(new MetricContainerDto());
		return module;
	}

}
