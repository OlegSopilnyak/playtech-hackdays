/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.controller;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.external.dto.ExternalModuleStateDto;
import oleg.sopilnyak.external.dto.GeneralModuleStateDto;
import oleg.sopilnyak.external.dto.ModuleStatusDto;
import oleg.sopilnyak.external.dto.RemoteModuleDto;
import oleg.sopilnyak.external.service.ModuleSystemFacade;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for modules operation
 */
@Slf4j
@RestController
@RequestMapping(value = ControllerConstant.MODULE_BASE)
public class ModuleSystemController {
	@Autowired
	ModuleSystemFacade facade;

	/**
	 * To get list of registered modules
	 *
	 * @return modules
	 */
	@GetMapping
	public ResponseEntity<List<String>> registeredModules() {
		final List<String> modules = facade.registeredModules();
		log.debug("Collected {} registered modules.", modules.size());
		return ResponseEntity.ok(modules);
	}

	/**
	 * To get status of registered module
	 *
	 * @param modulePK primaryKey of the module
	 * @return current module status
	 */
	@GetMapping(value = "/status")
	public ResponseEntity<ModuleStatusDto> moduleStatus(
			@RequestParam(name = ControllerConstant.MODULE_PARAMETER) final String modulePK
	) {
		log.debug("Getting status for module '{}'", modulePK);
		return ResponseEntity.ok(facade.moduleStatus(modulePK));
	}


	/**
	 * To start particular module
	 *
	 * @param modulePK PK of module to start
	 * @return new status of module
	 */
	@PutMapping(value = "/start")
	public ResponseEntity<ModuleStatusDto> moduleStart(
			@RequestParam(name = ControllerConstant.MODULE_PARAMETER) final String modulePK
	) {
		log.debug("Starting module '{}'", modulePK);
		return ResponseEntity.ok(facade.moduleStart(modulePK));
	}

	/**
	 * To stop particular module
	 *
	 * @param modulePK PK of module to stop
	 * @return new status of module
	 */
	@PutMapping(value = "/stop")
	public ResponseEntity<ModuleStatusDto> moduleStop(
			@RequestParam(name = ControllerConstant.MODULE_PARAMETER) final String modulePK
	) {
		log.debug("Stopping module '{}'", modulePK);
		return ResponseEntity.ok(facade.moduleStop(modulePK));
	}

	/**
	 * To register external module
	 *
	 * @param externalModule external module
	 * @return status of registered module
	 */
	@PostMapping(value = "/register")
	public ResponseEntity<ModuleStatusDto> registerExternalModule(RemoteModuleDto externalModule){
		log.debug("Try to register external module '{}'", externalModule.primaryKey());
		return ResponseEntity.ok(facade.registerModule(externalModule));
	}

	/**
	 * To unregister external module
	 *
	 * @param externalModule external module
	 * @return last status of the module
	 */
	@DeleteMapping(value = "/register")
	public ResponseEntity<ModuleStatusDto> unRegisterExternalModule(ModuleDto externalModule){
		log.debug("Try to un-register external module '{}'", externalModule.primaryKey());
		return ResponseEntity.ok(facade.unRegisterModule(externalModule));
	}

	/**
	 * To update state of external module
	 *
	 * @param state external state of registered module
	 * @return updated state of external module
	 */
	@PutMapping(value = "/ping")
	public ResponseEntity<GeneralModuleStateDto> updateModuleState(ExternalModuleStateDto state){
		log.debug("Updating state of external module '{}'", state.getModulePK());
		return ResponseEntity.ok(facade.status(state));
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<String> processError(Exception e) {
		return ResponseEntity.badRequest().body("Error:" + e.getMessage());
	}
}
