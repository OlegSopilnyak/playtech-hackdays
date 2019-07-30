/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.model;

import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.model.dto.ModuleActionDto;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for various data transfer objects
 */
@Mapper
public interface DtoMapper {
	DtoMapper INSTANCE = Mappers.getMapper( DtoMapper.class );

	/**
	 * To make a dto for module
	 *
	 * @param module source
	 * @return instance
	 */
	ModuleDto toModuleDto(ModuleBasics module);

	/**
	 * To restore DTO from primaryKey
	 *
	 * @param primaryKey
	 * @return instance
	 */
	default ModuleDto toModuleDto(String primaryKey){
		return new ModuleDto(primaryKey);
	}

	/**
	 * To make DTO for an action
	 *
	 * @param action source
	 * @return instance
	 */
	@Mapping(target = "module", expression = "java(action.getModule())" )
	ModuleActionDto toActionDto(ModuleAction action);
}
