/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service.impl;

import oleg.sopilnyak.commands.CommandResult;
import oleg.sopilnyak.commands.ModuleCommand;
import oleg.sopilnyak.commands.ModuleCommandFactory;
import oleg.sopilnyak.commands.model.ModuleCommandType;
import oleg.sopilnyak.commands.model.ModuleInfoAdapter;
import oleg.sopilnyak.external.dto.ModuleStatusDto;
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
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

	private Map<String, ExternalModuleImpl> sharedMap = spy(new HashMap<>());
	private DistributedExternalModulesFactoryImpl distributedExternalModulesFactory = spy(new DistributedExternalModulesFactoryImpl());

	@Spy
	@InjectMocks
	private ModuleSystemFacadeImpl facade = new ModuleSystemFacadeImpl();

	@Before
	public void setUp() {
		facade.distributedModules = distributedExternalModulesFactory;
		distributedExternalModulesFactory.sharedRegisteredModulesMap = sharedMap;
	}

	@After
	public void tearDown() {
		sharedMap.clear();
		reset(facade, commandFactory, registry, actionStorage, configurationStorage);
		reset(sharedMap, distributedExternalModulesFactory);
	}

	@Test
	public void testRegisteredModules() {
		List<ModuleInfoAdapter> registeredModules = new ArrayList<>();
		ModuleInfoAdapter module1 = mock(ModuleInfoAdapter.class);
		ModuleInfoAdapter module2 = mock(ModuleInfoAdapter.class);
		String module1PK = "test::test1::test";
		String module2PK = "test::test2::test";
		when(module1.getModulePK()).thenReturn(module1PK);
		when(module2.getModulePK()).thenReturn(module2PK);
		registeredModules.add(module1);
		registeredModules.add(module2);
		CommandResult result = mock(CommandResult.class);
		when(result.getData()).thenReturn(registeredModules);
		ModuleCommand list = mock(ModuleCommand.class);
		when(list.execute()).thenReturn(result);
		// adjusting commands factory
		when(commandFactory.create(ModuleCommandType.LIST)).thenReturn(list);
		// adjusting distributed modules factory
		String external = "test::test3::test";
		sharedMap.put(external, new ExternalModuleImpl());

		List<String> registered = facade.registeredModules();

		assertFalse(CollectionUtils.isEmpty(registered));
		assertTrue(registered.size() >= 3);
		assertEquals(module1PK, registered.get(0));
		assertEquals(module2PK, registered.get(1));
		assertEquals("ext::" + external, registered.get(2));

		verify(facade, times(1)).registeredModules();
		verify(commandFactory, times(1)).create(eq(ModuleCommandType.LIST));
		verify(list, times(1)).execute();
		verify(result, times(1)).getData();
		verify(module1, times(1)).getModulePK();
		verify(module2, times(1)).getModulePK();
		verify(distributedExternalModulesFactory, times(1)).registeredModules();
		verify(sharedMap, times(1)).keySet();
	}

	@Test
	public void testModuleStatus() {
		List<ModuleInfoAdapter> registeredModules = new ArrayList<>();
		ModuleInfoAdapter module1 = mock(ModuleInfoAdapter.class);
		ModuleInfoAdapter module2 = mock(ModuleInfoAdapter.class);
		String module1PK = "test::test1::test";
		String module2PK = "test::test2::test";
		when(module1.getModulePK()).thenReturn(module1PK);
		when(module2.getModulePK()).thenReturn(module2PK);
		registeredModules.add(module1);
		registeredModules.add(module2);
		CommandResult result = mock(CommandResult.class);
		when(result.getData()).thenReturn(registeredModules);
		ModuleCommand statusCommand = mock(ModuleCommand.class);
		when(statusCommand.execute(module1PK)).thenReturn(result);
		// adjusting commands factory
		when(commandFactory.create(ModuleCommandType.STATUS)).thenReturn(statusCommand);

		ModuleStatusDto status = facade.moduleStatus(module1PK);
		assertNotNull(status);
		assertEquals(module1PK, status.getModulePK());

		verify(facade, times(1)).moduleStatus(eq(module1PK));
		verify(facade, times(1)).executeSingleModuleCommand(eq(ModuleCommandType.STATUS), eq(module1PK));
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