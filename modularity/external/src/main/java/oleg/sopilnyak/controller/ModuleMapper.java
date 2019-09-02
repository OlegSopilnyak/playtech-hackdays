/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.controller;

import oleg.sopilnyak.commands.model.ModuleInfoAdapter;
import oleg.sopilnyak.dto.ModuleStatusDto;
import oleg.sopilnyak.module.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

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
}
