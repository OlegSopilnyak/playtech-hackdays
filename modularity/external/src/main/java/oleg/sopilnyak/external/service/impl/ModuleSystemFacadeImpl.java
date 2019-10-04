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
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.model.DtoMapper;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;

import java.util.*;
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
	@Qualifier("external-modules-map")
	Map<String, ExternalModule> sharedModulesMap;

	Map<ModuleBasics, ExternalModule> sharedExternalModules;
	@Autowired
	@Qualifier("registered-modules-map")
	Map<String, ExternalModuleImpl> sharedRegisteredModulesMap;

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

	private ExternalModule getOrCreateExternalModule(ModuleBasics externalModule, String moduleHost) {
		final Module registered = registry.getRegistered(externalModule);
		if (registered instanceof ExternalModule) {
			return (ExternalModule) registered;
		}
		final ModuleDto module = DtoMapper.INSTANCE.toModuleDto(externalModule);
		final ExternalModule external = sharedExternalModules.computeIfAbsent(module, (m) -> createExternalModule(m));
		if (external.valuesSize() == 0) {
			external.registeredFor(moduleHost);
			external.moduleStart();
			registry.add(external);
		}
		return external;
	}

	private ExternalModule createExternalModule(ModuleBasics m) {
		final ExternalModuleImpl module = new ExternalModuleImpl();
		module.setSystemId(m.getSystemId());
		module.setModuleId(m.getModuleId());
		module.setVersionId(m.getVersionId());
		module.setDescription(m.getDescription());
		module.setModuleValues(new LinkedHashMap<>());
		module.setMetricsContainer(new MetricContainerDto());
		module.moduleStart();
		return module;
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
		final ModuleStatusDto result = wrongModuleStatus(remoteModule);

		log.debug("GetOrCreate external module '{}' for host '{}'", remoteModule.primaryKey(), moduleHost);
		final ExternalModule external = getOrCreateExternalModule(remoteModule, moduleHost);

		log.debug("Making values of module '{}' for host '{}'", external.primaryKey(), moduleHost);
		final ModuleValuesDto values = createModuleValues(remoteModule, moduleHost);
		if (external.registerValues(values)) {

			log.debug("Creating MainAction for module");
			values.setMainAction(actionStorage.createActionFor(external));

			log.debug("Merging metrics...");
			external.getMetricsContainer().merge(remoteModule.getMetrics());

			log.debug("Updating distributed external-modules map.");
			sharedExternalModules.put(DtoMapper.INSTANCE.toModuleDto(external), external);
			ModuleMapper.INSTANCE.copyModuleStatus(result, values);
		}
		return result;


//		final String candidateModulePK = remoteModule.primaryKey();
//		log.debug("Try to register module '{}' for host '{}'", candidateModulePK, moduleHost);
//
//		// getting registered in registry module by module-pk
//		final Module registered = registry.getRegistered(candidateModulePK);
//		if (Objects.isNull(registered)) {
//			// module is not registered here maybe in shared
//		}
//		// getting shared module as registered
//		final ExternalModuleImpl externalRegistered = sharedRegisteredModulesMap.get(candidateModulePK);
//
//		final ExternalModule external = registerExternalModule(remoteModule, moduleHost);
//		if (Objects.nonNull(external)) {
//			final String modulePK = externalModulePK(remoteModule.primaryKey(), moduleHost);
//			log.debug("Registered external module : {}", remoteModule);
//			ModuleMapper.INSTANCE.copyModuleStatus(result, external);
//			sharedModulesMap.put(modulePK, external);
//		}
//		return result;
	}

	private ModuleValuesDto createModuleValues(RemoteModuleDto remoteModule, String moduleHost) {
		final ModuleValuesDto values = new ModuleValuesDto();
		values.setActive(remoteModule.isActive());
		values.setChanged(Collections.EMPTY_MAP);
		values.setCondition(remoteModule.getCondition());
		values.setHost(moduleHost);
		values.setConfiguration(new LinkedHashMap<>(remoteModule.getConfiguration()));
		return values;
	}

	private ModuleStatusDto wrongModuleStatus(ModuleBasics module) {
		final ModuleStatusDto result = new ModuleStatusDto();
		result.setModulePK(module.primaryKey());
		result.setActive(false);
		result.setCondition(ModuleHealthCondition.DAMAGED);
		return result;
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
		log.debug("Un-Registering '{}'", remoteModule);
		final ModuleStatusDto result = new ModuleStatusDto();
		result.setActive(false);
		result.setCondition(ModuleHealthCondition.DAMAGED);

		// getting module from registry
		final String modulePK = externalModulePK(remoteModule.primaryKey(), moduleHost);
		final Module module = registry.getRegistered(remoteModule);
		if (Objects.nonNull(module) && module instanceof ExternalModule) {
			// module is external, so we can un-register it
			registry.remove(module);
			sharedModulesMap.remove(modulePK);
			log.debug("Removed external module registration: {}", remoteModule);
			module.moduleStop();
//			ModuleMapper.INSTANCE.copyModuleStatus(result, (ExternalModule) module);
		} else {
			final ExternalModule remote = sharedModulesMap.get(modulePK);
			if (Objects.nonNull(remote)) {
				log.debug("External Module '{}' is not registered for this host, mark it as 'DAMAGED'", remote.primaryKey());
//				((ExternalModuleImpl) remote).setCondition(ModuleHealthCondition.DAMAGED);
				remote.moduleStop();
				sharedModulesMap.put(modulePK, remote);
			}
		}
		return result;
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
		final String modulePK = externalModulePK(externalState.getModulePK(), moduleHost);
		log.debug("Try to update status for '{}'", modulePK);

		final ExternalModule module = sharedModulesMap.get(modulePK);
		if (Objects.isNull(module)) {
			log.debug("External module '{}' is not found.", modulePK);
			throw new ModuleNotFoundException(modulePK);
		} else if (module.isDetached()) {
			log.debug("External module '{}' is detached from modules processing.", modulePK);
			sharedModulesMap.remove(modulePK);
			throw new ModuleNotFoundException(modulePK);
		}
		try {
			log.debug("Returning actual general state for external module '{}'", modulePK);
			final ModuleStatusDto status = ModuleMapper.INSTANCE.toStatusDto(module);
			return ModuleMapper.INSTANCE.toGeneralStateDto(status, module);
		} finally {
			log.debug("Merging main and changed configuration for external module '{}'", modulePK);
//			module.repairConfiguration();
			log.debug("Storing updated external module '{}' to distributed map", modulePK);
			sharedModulesMap.put(modulePK, module);
		}
	}

	// private methods
	boolean isRegisteredModule(ModuleBasics module, String moduleHost) {
		return sharedModulesMap.containsKey(externalModulePK(module.primaryKey(), moduleHost))
				|| Objects.nonNull(registry.getRegistered(module))
				;
	}

	ExternalModule registerExternalModule(RemoteModuleDto remoteModule, String moduleHost) {
		if (isRegisteredModule(remoteModule, moduleHost)) {
			log.warn("Module '{}' already registered.", remoteModule.primaryKey());
			return null;
		}

		// creating external module
		log.debug("Making external module instance");
//		final ExternalModule module = ModuleMapper.INSTANCE.toExternalModule(remoteModule, sharedModulesMap);

		// preparing module in host main-action
		// todo find better solution for pair module-host
//		log.debug("Preparing main action for external module '{}'", module.primaryKey());
//		final ModuleAction mainAction = actionStorage.createActionFor(module);
//		actionStorage.persist(mainAction);
//		module.setMainAction(DtoMapper.INSTANCE.toActionDto(mainAction));

		// store current module configuration
//		log.debug("Storing '{}' current configuration.", module.primaryKey());
		if (!CollectionUtils.isEmpty(remoteModule.getConfiguration())) {
			final Map<String, VariableItem> config = new LinkedHashMap<>();
			log.debug("Storing remote configuration {}", remoteModule.getConfiguration());
			remoteModule.getConfiguration().forEach((k, v) -> config.put(k, v));
//			((ExternalModuleImpl) module).setConfiguration(config);
		}
//		final Map<String, VariableItem> changed = configurationStorage.getUpdatedVariables(module, module.getConfiguration());
//		if (!CollectionUtils.isEmpty(changed)) {
//			log.debug("Storing difference configuration {}", changed);
//			((ExternalModuleImpl) module).setChanged(ModuleMapper.INSTANCE.toConfigurationDto(changed));
//		}

		// register external module in the registry
//		log.debug("Registering module '{}'", module.primaryKey());
//		registry.add(module);

		// returning registered external module
//		return module;
		return null;
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
