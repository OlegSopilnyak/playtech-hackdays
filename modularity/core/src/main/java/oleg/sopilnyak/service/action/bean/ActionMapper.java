/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.bean;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.ServiceModule;
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.action.bean.factory.ModuleMainAction;
import oleg.sopilnyak.service.action.bean.factory.ModuleRegularAction;
import oleg.sopilnyak.service.action.bean.result.FailModuleAction;
import oleg.sopilnyak.service.action.bean.result.SuccessModuleAction;
import oleg.sopilnyak.service.action.impl.ActionStorageWrapper;
import oleg.sopilnyak.service.model.DtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Objects;

/**
 * MapStruct mapper for various actions
 */
@Mapper(imports = {ModuleAction.class, DtoMapper.class, Objects.class})
public interface ActionMapper {
	ActionMapper INSTANCE = Mappers.getMapper(ActionMapper.class);

	/**
	 * To make simple instance of pure ModuleAction
	 *
	 * @param module the owner od action
	 * @param name the name of action
	 * @return instance
	 */
	@Mapping(target = "module", source = "module")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "state", expression = "java(ModuleAction.State.INIT)")
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "parent", ignore = true)
	@Mapping(target = "started", ignore = true)
	@Mapping(target = "duration", ignore = true)
	@Mapping(target = "hostName", ignore = true)
	ModuleActionAdapter simple(ModuleBasics module, String name);

	/**
	 * To make simple instance of pure ModuleAction
	 *
	 * @param module the owner od action
	 * @param name the name of action
	 * @param parent the parent of action
	 * @return instance
	 */
	@Mapping(target = "module", source = "module")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "state", expression = "java(ModuleAction.State.INIT)")
	@Mapping(target = "parent", source = "parent")
	@Mapping(target = "description", expression = "java(module.getDescription())")
	ModuleActionAdapter simple(ModuleBasics module, ModuleAction parent, String name);

	/**
	 * To make new instance of fail-result action
	 *
	 * @param action current action
	 * @param exception occurred exception
	 * @return instance
	 */
	@Mapping(target = "module", source = "action.module")
	@Mapping(target = "cause", source = "exception")
	FailModuleAction toFailResult(ModuleAction action, Throwable exception);

	/**
	 * To make instance of success-result action
	 *
	 * @param action current action
	 * @return instance
	 */
	@Mapping(target = "module", source = "action.module")
	@Mapping(target = "cause", expression = "java(null)")
	SuccessModuleAction toSuccessResult(ModuleAction action);

	/**
	 * To make instance of main-action for module
	 *
	 * @param module owner of action
	 * @param idGenerator generator of unique ids
	 * @param hostName network host's name
	 * @return instance
	 */
	@Mapping(target = "id", expression = "java(idGenerator.generate())")
	@Mapping(target = "module", expression = "java(DtoMapper.INSTANCE.toModuleDto(module))")
	@Mapping(target = "parent", expression = "java(null)")
	@Mapping(target = "name", expression = "java(\"[main->\" + module.getSystemId() + \"->\" + module.getModuleId() + \"]\")")
	@Mapping(target = "hostName", source = "hostName")
	@Mapping(target = "state", expression = "java(ModuleAction.State.INIT)")
	@Mapping(target = "started", expression = "java(null)")
	@Mapping(target = "duration", expression = "java(-1L)")
	@Mapping(target = "description", expression = "java(\"Main action of \" + module.getDescription())")
	ModuleMainAction toMainAction(Module module, UniqueIdGenerator idGenerator, String hostName);

	/**
	 * To make sub-action for the module
	 *
	 * @param module owner of action
	 * @param parent action where sub-action will live
	 * @param name the name of action
	 * @param idGenerator generator of unique ids
	 * @param hostName network host's name
	 * @return instance
	 */
	@Mapping(target = "id", expression = "java(idGenerator.generate())")
	@Mapping(target = "module", expression = "java(DtoMapper.INSTANCE.toModuleDto(module))")
	@Mapping(target = "parent", expression = "java(Objects.isNull(parent) ? module.getMainAction() : parent)")
	@Mapping(target = "name", expression = "java(\"[\"+name+\"-action]\")")
	@Mapping(target = "hostName", source = "hostName")
	@Mapping(target = "state", expression = "java(ModuleAction.State.INIT)")
	@Mapping(target = "started", expression = "java(null)")
	@Mapping(target = "duration", expression = "java(-1L)")
	@Mapping(target = "description", expression = "java(name + \" action of \" + module.getDescription())")
	ModuleRegularAction toRegularAction(ServiceModule module, ModuleAction parent, String name, UniqueIdGenerator idGenerator, String hostName);

	/**
	 * To make wrapper for ModuleAction to save
	 *
	 * @param action action to wrap
	 * @return wrapped action
	 */
	@Mapping(target = "module", expression = "java(DtoMapper.INSTANCE.toModuleDto(action.getModule()))")
	@Mapping(target = "parent", expression = "java(wrap(action.getParent(), true))")
	ActionStorageWrapper wrap(ModuleAction action, boolean deep);
}
