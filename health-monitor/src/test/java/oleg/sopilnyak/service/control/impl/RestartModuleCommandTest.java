/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import oleg.sopilnyak.configuration.ModuleCommandConfiguration;
import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.control.CommandResult;
import oleg.sopilnyak.service.control.ModuleCommand;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static oleg.sopilnyak.service.control.model.ModuleCommandState.SUCCESS;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RestartModuleCommandTest {
	@Spy
	private ObjectMapper mapper = new ModuleUtilityConfiguration().getObjectMapper();
	@Mock
	private ModulesRegistry registry;
	@Mock
	private ObjectFactory<HelpModuleCommand> helpCommandFactory;

	private HelpModuleCommand help = new ModuleCommandConfiguration().makeHelpModuleCommand();

	@InjectMocks
	private ModuleCommand command = new ModuleCommandConfiguration().makeRestartModuleCommand();

	@Before
	public void setUp() throws Exception {
		List<Module> modules = makeModules();
		when(registry.registered()).thenReturn(modules);
		when(helpCommandFactory.getObject()).thenReturn(help);
		ReflectionTestUtils.setField(help, "jsonMapper", mapper);
	}

	@After
	public void tearDown() throws Exception {
		reset(registry);
	}

	@Test
	public void testExecuteHelp() {

		CommandResult result = command.execute("help");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
	}

	@Test
	public void testExecuteNoParameters() {

		CommandResult result = command.execute();

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
	}

	@Test
	public void testExecuteParameterNoModule() {

		CommandResult result = command.execute("test");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
		assertEquals(0, ((List) result.getData()).size());
	}

	@Test
	public void testExecuteParameterOneModule() {

		CommandResult result = command.execute("1:2*");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
		assertEquals(1, ((List) result.getData()).size());
	}

	@Test
	public void testExecuteParameterTwoModules() {

		CommandResult result = command.execute("1:2:");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
		assertEquals(2, ((List) result.getData()).size());
	}

	@Test
	public void testExecuteParameterFourModules() {

		CommandResult result = command.execute("1:2:", "*", "1:22");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
		assertEquals(0, ((List) result.getData()).size());
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