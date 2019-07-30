/*
 * Copyright (C) Oleg Sopilnyak 2019
 */

package oleg.sopilnyak.service.configuration.storage;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.event.ExpandConfigurationEvent;
import oleg.sopilnyak.service.configuration.storage.event.ReplaceConfigurationEvent;
import oleg.sopilnyak.service.model.DtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MapStruct mapper for configuration-changed events
 */
@Mapper(imports = {DtoMapper.class, LinkedHashMap.class})
public interface ConfigurationMapper {
	ConfigurationMapper INSTANCE = Mappers.getMapper(ConfigurationMapper.class);

	/**
	 * To make event for expand the module's configuration
	 *
	 * @param module owner of configuration
	 * @param configuration configurations to add
	 * @return instance
	 */
	@Mapping(target = "module", expression = "java(DtoMapper.INSTANCE.toModuleDto(module))")
	@Mapping(target = "configuration", expression = "java(new LinkedHashMap(configuration))")
	ExpandConfigurationEvent toExpandEvent(Module module, Map<String, VariableItem> configuration);

	/**
	 * To make event for replace the module's configuration
	 *
	 * @param module owner of configuration
	 * @param configuration configurations to replace
	 * @return instance
	 */
	@Mapping(target = "module", expression = "java(DtoMapper.INSTANCE.toModuleDto(module))")
	@Mapping(target = "configuration", expression = "java(new LinkedHashMap(configuration))")
	ReplaceConfigurationEvent toReplaceEvent(Module module, Map<String, VariableItem> configuration);
}
