/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.control.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.control.CommandResult;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static oleg.sopilnyak.service.control.model.ModuleCommandState.SUCCESS;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StatusModuleCommandTest {
	@Spy
	private ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "$type")

//			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
			;
	@Mock
	private ModulesRegistry registry;
	@InjectMocks
	private StatusModuleCommand command = new StatusModuleCommand();

	private int counter = 0;

	@Before
	public void setUp() throws Exception {
		List<Module> modules = makeModules();
		when(registry.registered()).thenReturn(modules);
	}

	@After
	public void tearDown() throws Exception {
		reset(registry);
		counter = 0;
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
		Map<String, VariableItem> config = config();
		when(module.getConfiguration()).thenReturn(config);
		ModuleAction action = action(module);
		when(module.getMainAction()).thenReturn(action);
		return module;
	}

	private ModuleAction action(Module module) {
		ModuleAction action = mock(ModuleAction.class);
		when(action.getName()).thenReturn("[main-action]");
		when(action.getHostName()).thenReturn("favorite-host.com");
		when(action.getStarted()).thenReturn(Instant.now());
		when(action.getDuration()).thenReturn(counter * 1000L);
		when(action.getState()).thenReturn(ModuleAction.State.PROGRESS);
		return action;
	}

	private Map<String, VariableItem> config() {
		Map<String, VariableItem> config = new HashMap<>();
		if (counter++ % 2 == 0) {
			config.put("1.2.3." + counter, new VariableItemDto("demo", counter));
			config.put("3.4.5." + counter, new VariableItemDto("demo2", counter));
		}
		return config;
	}
}