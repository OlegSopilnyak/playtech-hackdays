/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.configuration;

import oleg.sopilnyak.external.service.ModuleSystemFacade;
import oleg.sopilnyak.external.service.impl.ModuleSystemFacadeImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration: external module main configuration
 */
@Configuration
@ComponentScan(basePackages = "oleg.sopilnyak.external.controller")
public class ExternalModuleConfiguration {

	@Bean
	public ModuleSystemFacade makeModuleSystemFacade(){
		return new ModuleSystemFacadeImpl();
	}
}
