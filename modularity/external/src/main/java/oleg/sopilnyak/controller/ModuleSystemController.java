/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.controller;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.dto.ModuleStatusDto;
import oleg.sopilnyak.service.ModuleSystemFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static oleg.sopilnyak.controller.ControllerConstant.MODULE_BASE;
import static oleg.sopilnyak.controller.ControllerConstant.MODULE_PARAMETER;

/**
 * Controller for modules operation
 */
@Slf4j
@RestController
@RequestMapping(value = MODULE_BASE)
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
			@RequestParam(name = MODULE_PARAMETER) final String modulePK
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
			@RequestParam(name = MODULE_PARAMETER) final String modulePK
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
			@RequestParam(name = MODULE_PARAMETER) final String modulePK
	) {
		log.debug("Stopping module '{}'", modulePK);
		return ResponseEntity.ok(facade.moduleStop(modulePK));
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<String> processError(Exception e) {
		return ResponseEntity.badRequest().body("Error:" + e.getMessage());
	}
}
