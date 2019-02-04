/**
 * Copyright (C) Oleg Sopilnyak 2018
 */

package oleg.sopilnyak.service.control.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import oleg.sopilnyak.configuration.ModuleCommandConfiguration;
import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.control.CommandResult;
import oleg.sopilnyak.service.control.ModuleCommand;
import oleg.sopilnyak.service.dto.VariableItemDto;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static oleg.sopilnyak.service.control.model.ModuleCommandState.FAIL;
import static oleg.sopilnyak.service.control.model.ModuleCommandState.SUCCESS;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ChangeConfigurationModuleCommandTest {
	@Spy
	private ObjectMapper mapper = new ModuleUtilityConfiguration().getObjectMapper();
	@Mock
	private ModulesRegistry registry;
	@Mock
	private ModuleConfigurationStorage storage;

	@InjectMocks
	private ModuleCommand command = new ModuleCommandConfiguration().makeChangeConfigurationModuleCommand();

	@Before
	public void setUp() throws Exception {
		List<Module> modules = makeModules();
		when(registry.registered()).thenReturn(modules);
		when(registry.getRegistered("1:2:3")).thenReturn(modules.get(0));
		Map<String, VariableItem> configuration = new HashMap<>();
		configuration.put("1.2.3.4", new VariableItemDto("4", 200));
		configuration.put("1.2.3.5", new VariableItemDto("5", "Snow"));
		when(modules.get(0).getConfiguration()).thenReturn(configuration);
	}

	@After
	public void tearDown() throws Exception {
		reset(registry);
	}

	@Test
	public void testChangeModuleConfigurationGood() {
		CommandResult result = command.execute("1:2:3", "1.2.3.4", "100");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
		final Module module = registry.registered().iterator().next();
		Map<String, VariableItem> configuration = module.getConfiguration();
		verify(registry, times(1)).getRegistered(eq("1:2:3"));
		verify(storage, times(1)).updateConfiguration(eq(module), eq(configuration));
	}

	@Test
	public void testChangeModuleConfigurationBad1() {
		CommandResult result = command.execute("1:2:3", "1.2.3.4", "Water");

		assertNotNull(result);
		assertEquals(FAIL, result.getState());
		assertTrue(result.getData() instanceof IllegalArgumentException);
		((IllegalArgumentException)result.getData()).printStackTrace(System.out);
		final Module module = registry.registered().iterator().next();
		Map<String, VariableItem> configuration = module.getConfiguration();
		verify(registry, times(1)).getRegistered(eq("1:2:3"));
		verify(storage, times(0)).updateConfiguration(eq(module), eq(configuration));
	}

	@Test
	public void testChangeModuleConfigurationBad2() {
		CommandResult result = command.execute("1:2:3", "1.2.3.44", "Water");

		assertNotNull(result);
		assertEquals(FAIL, result.getState());
		assertTrue(result.getData() instanceof IllegalArgumentException);
		((IllegalArgumentException)result.getData()).printStackTrace(System.out);
		final Module module = registry.registered().iterator().next();
		Map<String, VariableItem> configuration = module.getConfiguration();
		verify(registry, times(1)).getRegistered(eq("1:2:3"));
		verify(storage, times(0)).updateConfiguration(eq(module), eq(configuration));
	}

	@Test
	public void testChangeModuleConfigurationBad3() {
		CommandResult result = command.execute("1:2:3", "1.2.3.4");

		assertNotNull(result);
		assertEquals(FAIL, result.getState());
		assertTrue(result.getData() instanceof ArrayIndexOutOfBoundsException);
		((ArrayIndexOutOfBoundsException)result.getData()).printStackTrace(System.out);
		final Module module = registry.registered().iterator().next();
		Map<String, VariableItem> configuration = module.getConfiguration();
		verify(registry, times(0)).getRegistered(eq("1:2:3"));
		verify(storage, times(0)).updateConfiguration(eq(module), eq(configuration));
	}

	// private methods
	private List<Module> makeModules() {
		List<Module> modules = new ArrayList<>();
		modules.add(mockModule("1:2:3", true, ModuleHealthCondition.VERY_GOOD, "123"));
		modules.add(mockModule("1:2:33", true, ModuleHealthCondition.POOR, "123-123"));
		modules.add(mockModule("1:22:33", true, ModuleHealthCondition.POOR, "12223-123"));
		modules.add(mockModule("1:22:3-3", true, ModuleHealthCondition.VERY_GOOD, "1-2-3"));
		modules.add(mockModule("1:2*2:3*3", false, ModuleHealthCondition.FAIL, "1*2*3"));
		return modules;
	}

	private Module mockModule(String pk, boolean active, ModuleHealthCondition condition, String description) {
		Module module = mock(Module.class);
		when(module.primaryKey()).thenReturn(pk);
		when(module.isActive()).thenReturn(active);
		when(module.getCondition()).thenReturn(condition);
		when(module.getDescription()).thenReturn(description);
		return module;
	}
}