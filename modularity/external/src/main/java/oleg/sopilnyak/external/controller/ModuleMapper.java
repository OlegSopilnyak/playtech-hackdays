/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.controller;

import oleg.sopilnyak.commands.model.ModuleInfoAdapter;
import oleg.sopilnyak.external.dto.GeneralModuleStateDto;
import oleg.sopilnyak.external.dto.ModuleStatusDto;
import oleg.sopilnyak.external.service.ExternalModule;
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
	@Mapping(target = "active", ignore = true)
	@Mapping(target = "condition", ignore = true)
	@Mapping(target = "modulePK", expression = "java(module.primaryKey())" )
	ModuleStatusDto toStatusDto(ExternalModule module);

	/**
	 * Copy status of values to dto-object
	 *
	 * @param dto dto-object of status
	 * @param values values-values
	 */
	@Mapping(target = "modulePK", ignore = true)
	@Mapping(target = "description", ignore = true)
	void copyModuleStatus(@MappingTarget ModuleStatusDto dto, ModuleValues values);

	/**
	 * To create full information module's status DTO
	 *
	 * @param status status of module
	 * @param module external module instance
	 * @return instance of full module state DTO
	 */
	@Mapping(target = "mainActionId", ignore = true)
	@Mapping(target = "configuration", ignore = true)
	@Mapping(target = "description", source = "status.description")
	@Mapping(target = "active", source = "status.active")
	@Mapping(target = "condition", source = "status.condition")
	GeneralModuleStateDto toGeneralStateDto(ModuleStatusDto status, ExternalModule module);


}
