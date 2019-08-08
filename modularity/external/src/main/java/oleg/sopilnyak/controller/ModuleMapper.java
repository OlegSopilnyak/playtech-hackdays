/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.controller;

import oleg.sopilnyak.commands.model.ModuleInfoAdapter;
import oleg.sopilnyak.dto.ModuleStatusDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for various data transfer objects
 */
@Mapper
public interface ModuleMapper {
	ModuleMapper INSTANCE = Mappers.getMapper( ModuleMapper.class );

	ModuleStatusDto toStatusDto(ModuleInfoAdapter module);
}
