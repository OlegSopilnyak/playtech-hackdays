/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.commands.CommandResult;
import oleg.sopilnyak.commands.ModuleCommandFactory;
import oleg.sopilnyak.commands.model.ModuleCommandType;
import oleg.sopilnyak.commands.model.ModuleInfoAdapter;
import oleg.sopilnyak.external.controller.ModuleMapper;
import oleg.sopilnyak.external.dto.*;
import oleg.sopilnyak.external.exception.ModuleNotFoundException;
import oleg.sopilnyak.external.service.ExternalModule;
import oleg.sopilnyak.external.service.ModuleSystemFacade;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.ModuleValues;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Facade: working with remote (external) modules realization
 *
 * @see ModuleSystemFacade
 */
@Slf4j
public class ModuleSystemFacadeImpl implements ModuleSystemFacade {
	@Autowired
	ModuleCommandFactory commandFactory;
	@Autowired
	ModulesRegistryService registry;
	@Autowired
	ModuleActionStorage actionStorage;

	// storage of all modules configurations
	@Autowired
	ModuleConfigurationStorage configurationStorage;

	// distributed map of registered external modules
	@Autowired
	@Qualifier("registered-modules-map")
	Map<String, ExternalModuleImpl> sharedRegisteredModulesMap;

	// duration for detect detaching of external module
	long moduleExpiredDuration = TimeUnit.MINUTES.toMillis(10);

	/**
	 * To get list of registered modules
	 *
	 * @return list of registered modules
	 */
	@Override
	public List<String> registeredModules() {
		final CommandResult result = commandFactory.create(ModuleCommandType.LIST).execute();
		final List<ModuleInfoAdapter> modules = (List<ModuleInfoAdapter>) result.getData();
		log.debug("Collected {} registered modules.", modules.size());
		return modules.stream().map(mi -> mi.getModulePK()).collect(Collectors.toList());
	}

	/**
	 * To get the status of particular module
	 *
	 * @param modulePK Primary Key of module
	 * @return module's status
	 */
	@Override
	public ModuleStatusDto moduleStatus(String modulePK) {
		final ModuleInfoAdapter moduleInfo = executeSingModuleCommand(ModuleCommandType.STATUS, modulePK);
		return ModuleMapper.INSTANCE.toStatusDto(moduleInfo);
	}

	/**
	 * Try to start particular module
	 *
	 * @param modulePK Primary Key of module
	 * @return module's status
	 */
	@Override
	public ModuleStatusDto moduleStart(String modulePK) {
		final ModuleInfoAdapter moduleInfo = executeSingModuleCommand(ModuleCommandType.START, modulePK);
		return ModuleMapper.INSTANCE.toStatusDto(moduleInfo);
	}

	/**
	 * Try to stop particular module
	 *
	 * @param modulePK Primary Key of module
	 * @return module's status
	 */
	@Override
	public ModuleStatusDto moduleStop(String modulePK) {
		final ModuleInfoAdapter moduleInfo = executeSingModuleCommand(ModuleCommandType.STOP, modulePK);
		return ModuleMapper.INSTANCE.toStatusDto(moduleInfo);
	}

	/**
	 * To register external module
	 *
	 * @param remoteModule remote module
	 * @param moduleHost   owner of remote module
	 * @return status of registered module
	 */
	@Override
	public ModuleStatusDto registerModule(RemoteModuleDto remoteModule, String moduleHost) {
		final String modulePK = remoteModule.primaryKey();
		final ModuleStatusDto moduleStatus = wrongModuleStatus(remoteModule);
		log.debug("Try to register external module '{}' for host '{}'", modulePK, moduleHost);

		log.debug("GetOrCreate external module '{}' for host '{}'", modulePK, moduleHost);
		final ExternalModuleImpl external = getOrCreateExternalModule(remoteModule);

		log.debug("Making values of module '{}' for host '{}'", modulePK, moduleHost);
		final ModuleValuesDto values = createModuleValues(remoteModule, moduleHost);
		if (external.registerValues(values)) {
			log.debug("Registering values of module '{}' for host '{}'", modulePK, moduleHost);

			log.debug("Creating MainAction for module");
			values.setMainAction(actionStorage.createActionFor(external));

			log.debug("Merging metrics...");
			external.getMetricsContainer().merge(remoteModule.getMetrics());

			log.debug("Updating distributed external-modules map.");
			sharedRegisteredModulesMap.put(modulePK, external);
			ModuleMapper.INSTANCE.copyModuleStatus(moduleStatus, values);
		}
		return moduleStatus;
	}

	/**
	 * To un-register external module
	 *
	 * @param remoteModule remote module
	 * @param moduleHost   owner of remote module
	 * @return last status of module
	 */
	@Override
	public ModuleStatusDto unRegisterModule(ModuleDto remoteModule, String moduleHost) {
		final String modulePK = remoteModule.primaryKey();
		final ModuleStatusDto moduleStatus = wrongModuleStatus(remoteModule);
		log.debug("Un-Registering '{}' for host '{}'", modulePK, moduleHost);

		final ExternalModuleImpl external = sharedRegisteredModulesMap.get(modulePK);
		if (Objects.nonNull(external)) {
			unRegisterExistingExternalModule(moduleHost, modulePK, moduleStatus, external);
		} else {
			log.warn("Not found external module '{}'", modulePK);
		}
		return moduleStatus;
	}

	/**
	 * To update status of external module
	 *
	 * @param externalState remote state of external module
	 * @param moduleHost    owner of remote module
	 * @return updated state of external module (include module configuration updates)
	 */
	@Override
	public GeneralModuleStateDto status(ExternalModuleStateDto externalState, String moduleHost) {
		final String modulePK = externalState.getModulePK();
		final ExternalModuleImpl external = sharedRegisteredModulesMap.get(modulePK);
		if (Objects.isNull(external)) {
			log.debug("External module '{}' is not found.", modulePK);
			throw new ModuleNotFoundException(modulePK);
		}
		final boolean isInRegistry = Objects.nonNull(registry.getRegistered(modulePK));
		if (!external.hasValues()) {
			return moduleHasNoValues(modulePK, external, isInRegistry);
		} else {
			testingModuleState(modulePK, external, isInRegistry);
		}

		return makeExternalModuleGeneralStatus(moduleHost, modulePK, external);
	}


	/**
	 * To check is module registered correctly
	 *
	 * @param external module to check
	 * @return true if registered correctly
	 */
	boolean isModuleRegistered(final ExternalModule external) {
		if (Objects.isNull(registry.getRegistered(external.primaryKey()))) {
			return false;
		} else if (!actionStorage.getHostName().equals(external.registryIn())) {
			log.debug("Module '{}' registered in another host, unregister from here.");
			registry.remove(external);
			return false;
		} else {
			return true;
		}
	}

	// private methods
	void unRegisterExistingExternalModule(String moduleHost, String modulePK, ModuleStatusDto moduleStatus, ExternalModuleImpl external) {
		log.debug("Removing values for host '{}'", moduleHost);
		final ModuleValues values = external.valuesFor(moduleHost);
		if (Objects.nonNull(values)) {
			external.unRegisterValues(values);
			sharedRegisteredModulesMap.put(modulePK, external);
			ModuleMapper.INSTANCE.copyModuleStatus(moduleStatus, values);
		} else {
			log.warn("No registered values of module '{}' for host '{}'", modulePK, moduleHost);
		}
		if (!external.hasValues() && Objects.nonNull(registry.getRegistered(modulePK))) {
			registry.remove(external);
			sharedRegisteredModulesMap.remove(modulePK);
			log.debug("Module '{}' un-registered for all hosts completely.", modulePK);
		}
	}

	GeneralModuleStateDto makeExternalModuleGeneralStatus(String moduleHost, String modulePK, ExternalModuleImpl external) {
		final ModuleValuesDto values = (ModuleValuesDto) external.valuesFor(moduleHost);
		if (Objects.isNull(values)) {
			log.warn("Module '{}' hasn't values for host '{}'", moduleHost);
			throw new ModuleNotFoundException(externalModulePK(modulePK, moduleHost));
		}
		final ModuleStatusDto status = wrongModuleStatus(external);
		ModuleMapper.INSTANCE.copyModuleStatus(status, values);
		final GeneralModuleStateDto generalStatus = ModuleMapper.INSTANCE.toGeneralStateDto(status, external);

		log.debug("Processing configuration for host '{}'", moduleHost);
		values.repairConfiguration();
		sharedRegisteredModulesMap.put(modulePK, external);
		return generalStatus;
	}

	void testingModuleState(String modulePK, ExternalModuleImpl external, boolean isInRegistry) {
		log.debug("Testing the state of external module '{}'", modulePK);
		final String registeredIn = external.registryIn();
		if (isInRegistry) {
			if (!actionStorage.getHostName().equals(registeredIn)) {
				log.debug("Module '{}' registered in another host, unregister from here.");
				registry.remove(external);
			}
		} else {
			log.debug("Checking module touch timeout");
			if (external.isExpired(moduleExpiredDuration)) {
				log.debug("Register module here.");
				external.moduleStop();
				registerModule(external);
				sharedRegisteredModulesMap.put(modulePK, external);
			}
		}
	}

	GeneralModuleStateDto moduleHasNoValues(String modulePK, ExternalModuleImpl external, boolean isInRegistry) {
		log.debug("External module '{}' hasn't values at all", modulePK);
		sharedRegisteredModulesMap.remove(modulePK);
		if (isInRegistry) {
			registry.remove(external);
			log.debug("Module '{}' un-registered for all hosts completely.", modulePK);
			throw new ModuleNotFoundException(modulePK);
		} else {
			final ModuleStatusDto status = wrongModuleStatus(external);
			return ModuleMapper.INSTANCE.toGeneralStateDto(status, external);
		}
	}

	void registerModule(ExternalModuleImpl module) {
		log.debug("Registering external module '{}'", module.primaryKey());
		module.registryIn(actionStorage.getHostName());
		module.setFacadeImpl(this);
		module.moduleStart();
		registry.add(module);
	}

	ExternalModuleImpl getOrCreateExternalModule(final ModuleBasics remote) {
		final Module registered = registry.getRegistered(remote);
		if (registered instanceof ExternalModuleImpl) {
			return (ExternalModuleImpl) registered;
		}
		final Function<String, ExternalModuleImpl> createModuleFunction = (pk) -> createExternalModule(remote);
		final ExternalModuleImpl external = sharedRegisteredModulesMap.computeIfAbsent(remote.primaryKey(), createModuleFunction);
		if (!external.hasValues()) {
			registerModule(external);
		}
		return external;
	}

	ExternalModuleImpl createExternalModule(ModuleBasics m) {
		final ExternalModuleImpl module = new ExternalModuleImpl();
		module.setSystemId(m.getSystemId());
		module.setModuleId(m.getModuleId());
		module.setVersionId(m.getVersionId());
		module.setDescription(m.getDescription());
		module.setModuleValues(new LinkedHashMap<>());
		module.setMetricsContainer(new MetricContainerDto());
		return module;
	}

	ModuleValuesDto createModuleValues(RemoteModuleDto remoteModule, String moduleHost) {
		final ModuleValuesDto values = new ModuleValuesDto();
		values.setActive(remoteModule.isActive());
		values.setChanged(Collections.EMPTY_MAP);
		values.setCondition(remoteModule.getCondition());
		values.setHost(moduleHost);
		values.setConfiguration(new LinkedHashMap<>(remoteModule.getConfiguration()));
		return values;
	}

	ModuleStatusDto wrongModuleStatus(ModuleBasics module) {
		final ModuleStatusDto result = new ModuleStatusDto();
		result.setModulePK(module.primaryKey());
		result.setActive(false);
		result.setCondition(ModuleHealthCondition.DAMAGED);
		return result;
	}

	ModuleInfoAdapter executeSingModuleCommand(ModuleCommandType command, String modulePK) {
		log.debug("Executing module-command '{}' for '{}'", command, modulePK);
		final CommandResult result = commandFactory.create(command).execute(modulePK);
		final List<ModuleInfoAdapter> modules = (List<ModuleInfoAdapter>) result.getData();

		if (modules.isEmpty()) {
			log.error("Module not found pk:'{}'", modulePK);
			throw new ModuleNotFoundException(modulePK);
		}
		return modules.get(0);
	}
}
