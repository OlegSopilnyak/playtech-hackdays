/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.controller;

import oleg.sopilnyak.commands.model.ModuleInfoAdapter;
import oleg.sopilnyak.dto.ModuleStatusDto;
import oleg.sopilnyak.dto.RemoteModuleDto;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.service.ExternalModule;
import oleg.sopilnyak.service.impl.ExternalModuleImpl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Map;

/**
 * MapStruct mapper for various data transfer objects
 */
@Mapper
public interface ModuleMapper {
	ModuleMapper INSTANCE = Mappers.getMapper( ModuleMapper.class );

	/**
	 * Make status dto from module
	 *
	 * @param module result of module command
	 * @return dto object
	 */
	ModuleStatusDto toStatusDto(ModuleInfoAdapter module);

	/**
	 * Copy status of module to dto-object
	 *
	 * @param dto dto-object of status
	 * @param module module-source of data
	 */
	@Mapping(target = "modulePK", expression = "java(module.primaryKey())" )
	void copyModuleStatus(@MappingTarget ModuleStatusDto dto, Module module);

	/**
	 * Make external module by request
	 *
	 * @param remoteModule request to register remote module
	 * @param sharedModulesMap distributed map of registereed external modules
	 * @return external module instance
	 */
	ExternalModuleImpl toExternalModule(RemoteModuleDto remoteModule, Map<String, ExternalModule> sharedModulesMap);

	/**
	 * To copy parameters from distributed map to local module
	 *
	 * @param externalModule local external module
	 * @param shared external module from distributed map
	 */
	@Mapping(ignore = true, target = "metrics")
	@Mapping(ignore = true, target = "sharedModulesMap")
	void copyExternalModule(@MappingTarget ExternalModuleImpl externalModule, ExternalModule shared);
}
