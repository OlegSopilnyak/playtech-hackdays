/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.configuration;

import oleg.sopilnyak.service.ModuleSystemFacade;
import oleg.sopilnyak.service.impl.ModuleSystemFacadeImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration: external module main configuration
 */
@Configuration
@ComponentScan(basePackages = "oleg.sopilnyak.controller")
public class ExternalModuleConfiguration {

	@Bean
	public ModuleSystemFacade makeModuleSystemFacade(){
		return new ModuleSystemFacadeImpl();
	}
}
