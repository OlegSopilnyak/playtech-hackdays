/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service.impl;

import oleg.sopilnyak.commands.ModuleCommandFactory;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.reset;

@RunWith(MockitoJUnitRunner.class)
public class ModuleSystemFacadeImplTest {
	@Mock
	private ModuleCommandFactory commandFactory;
	@Mock
	private ModulesRegistryService registry;
	@Mock
	private ModuleActionStorage actionStorage;
	@Mock
	private ModuleConfigurationStorage configurationStorage;

	@Spy
	private Map<String, ExternalModuleImpl> sharedMap = new HashMap<>();
	@Spy
	@InjectMocks
	private DistributedExternalModulesFactoryImpl factory = new DistributedExternalModulesFactoryImpl();

	@Spy
	@InjectMocks
	private ModuleSystemFacadeImpl facade = new ModuleSystemFacadeImpl();

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
		reset(facade, commandFactory, registry, actionStorage, configurationStorage);
		reset(sharedMap, factory);
	}

	@Test
	public void registeredModules() {
	}

	@Test
	public void moduleStatus() {
	}

	@Test
	public void moduleStart() {
	}

	@Test
	public void moduleStop() {
	}

	@Test
	public void registerModule() {
	}

	@Test
	public void unRegisterModule() {
	}

	@Test
	public void status() {
	}

	@Test
	public void isValidModule() {
	}

	@Test
	public void unRegisterExistingExternalModule() {
	}

	@Test
	public void makeExternalModuleGeneralStatus() {
	}

	@Test
	public void testingModuleState() {
	}

	@Test
	public void moduleHasNoValues() {
	}

	@Test
	public void registeredModule() {
	}

	@Test
	public void getOrCreateExternalModule() {
	}

	@Test
	public void createModuleValues() {
	}

	@Test
	public void wrongModuleStatus() {
	}

	@Test
	public void executeSingleModuleCommand() {
	}

}