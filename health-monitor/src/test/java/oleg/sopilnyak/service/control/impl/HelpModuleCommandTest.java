/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import oleg.sopilnyak.configuration.ModuleCommandConfiguration;
import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.service.control.CommandResult;
import oleg.sopilnyak.service.control.ModuleCommand;
import oleg.sopilnyak.service.control.ModuleCommandFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import static oleg.sopilnyak.service.control.model.ModuleCommandState.SUCCESS;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HelpModuleCommandTest {
	@Spy
	private ObjectMapper mapper = new ModuleUtilityConfiguration().getObjectMapper();
	@Spy
	private ModuleCommandFactory commandsFactory = new ModuleCommandConfiguration().makeModuleCommandFactory();
	@Mock
	private ApplicationContext spring;
	@InjectMocks
	private ModuleCommand command = new ModuleCommandConfiguration().makeHelpModuleCommand();

	@Before
	public void setUp() throws Exception {
		ListModuleCommand list = new ModuleCommandConfiguration().makeListModuleCommand();
		ChangeConfigurationModuleCommand change = new ModuleCommandConfiguration().makeChangeConfigurationModuleCommand();
		StatusModuleCommand status = new ModuleCommandConfiguration().makeStatusModuleCommand();
		StartModuleCommand start = new ModuleCommandConfiguration().makeStartModuleCommand();
		StopModuleCommand stop = new ModuleCommandConfiguration().makeStopModuleCommand();
		RestartModuleCommand restart = new ModuleCommandConfiguration().makeRestartModuleCommand();
		when(spring.getBean(ListModuleCommand.class)).thenReturn(list);
		when(spring.getBean(ChangeConfigurationModuleCommand.class)).thenReturn(change);
		when(spring.getBean(StatusModuleCommand.class)).thenReturn(status);
		when(spring.getBean(StartModuleCommand.class)).thenReturn(start);
		when(spring.getBean(StopModuleCommand.class)).thenReturn(stop);
		when(spring.getBean(RestartModuleCommand.class)).thenReturn(restart);
		when(spring.getBean(HelpModuleCommand.class)).thenReturn((HelpModuleCommand)command);
		ReflectionTestUtils.setField(commandsFactory, "spring", spring);
	}

	@After
	public void tearDown() throws Exception {
		reset(spring);
	}

	@Test
	public void testCommandsList(){
		CommandResult result = command.execute();

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
	}

	@Test
	public void testingCommandList(){
		CommandResult result = command.execute("list");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
	}

	@Test
	public void testingCommandHelp(){
		CommandResult result = command.execute("help");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
	}

	@Test
	public void testingCommandRestart(){
		CommandResult result = command.execute("restart");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
	}

	@Test
	public void testingCommandStart(){
		CommandResult result = command.execute("start");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
	}

	@Test
	public void testingCommandStop(){
		CommandResult result = command.execute("stop");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
	}

	@Test
	public void testingCommandStatus(){
		CommandResult result = command.execute("status");

		assertNotNull(result);
		assertEquals(SUCCESS, result.getState());
		String tty = result.dataAsTTY();
		assertFalse(StringUtils.isEmpty(tty));
		String json = result.dataAsJSON();
		assertFalse(StringUtils.isEmpty(json));
	}
}