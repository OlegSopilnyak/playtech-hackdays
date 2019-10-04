/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.controller;

import oleg.sopilnyak.commands.model.ModuleInfoAdapter;
import oleg.sopilnyak.external.dto.GeneralModuleStateDto;
import oleg.sopilnyak.external.dto.ModuleStatusDto;
import oleg.sopilnyak.external.dto.RemoteModuleDto;
import oleg.sopilnyak.external.service.ExternalModule;
import oleg.sopilnyak.external.service.impl.ExternalModuleImpl;
import oleg.sopilnyak.module.ModuleValues;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.model.dto.VariableItemDto;
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

	@Mapping(target = "name", expression = "java(item.name())")
	@Mapping(target = "type", expression = "java(item.type())")
	@Mapping(target = "strategy", expression = "java(item.strategy())")
	@Mapping(target = "valueAsString", expression = "java(item.valueAsString())")
	VariableItemDto toVariableDto(VariableItem item);

	Map<String,VariableItemDto> toConfigurationDto(Map<String,VariableItem> configuration);
	/**
	 * Make status dto from module
	 *
	 * @param module result of module command
	 * @return dto object
	 */
	ModuleStatusDto toStatusDto(ModuleInfoAdapter module);

	/**
	 * To build status for external module
	 *
	 * @param module module instance
	 * @return trivial module status
	 */
	@Mapping(target = "modulePK", expression = "java(module.primaryKey())" )
	ModuleStatusDto toStatusDto(ExternalModule module);

	/**
	 * Copy status of module to dto-object
	 *
	 * @param dto dto-object of status
	 * @param module module-values
	 */
	void copyModuleStatus(@MappingTarget ModuleStatusDto dto, ModuleValues module);

	/**
	 * To create full information module's status DTO
	 *
	 * @param status status of module
	 * @param module external module instance
	 * @return instance of full module state DTO
	 */
//	@Mapping(target = "mainActionId", source = "module.mainAction.id")
	@Mapping(target = "mainActionId", ignore = true)
//	@Mapping(target = "configuration", expression = "java(module.getChanged())")
	@Mapping(target = "configuration", ignore = true)
	@Mapping(target = "description", source = "status.description")
	@Mapping(target = "active", source = "status.active")
	@Mapping(target = "condition", source = "status.condition")
	GeneralModuleStateDto toGeneralStateDto(ModuleStatusDto status, ExternalModule module);

	/**
	 * Make external module by request
	 *
	 * @param remoteModule request to register remote module
	 * @param sharedModulesMap distributed map of registered external modules
	 * @return external module instance
	 */
//	@Mapping(ignore = true, target = "mainAction")
//	@Mapping(ignore = true, target = "configuration")
//	@Mapping(ignore = true, target = "changed")
	ExternalModuleImpl toExternalModule(RemoteModuleDto remoteModule, Map<String, ExternalModule> sharedModulesMap);

	/**
	 * To copy parameters from distributed map to local module
	 *
	 * @param externalModule local external module
	 * @param shared external module from distributed map
	 */
//	@Mapping(ignore = true, target = "metrics")
//	@Mapping(target = "configuration", expression = "java(shared.getConfiguration())")
//	@Mapping(target = "changed", expression = "java(shared.getChanged())")
	void copyExternalModule(@MappingTarget ExternalModuleImpl externalModule, ExternalModule shared);
}
