/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import oleg.sopilnyak.external.dto.*;
import oleg.sopilnyak.external.service.ModuleSystemFacade;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.service.model.dto.ModuleActionDto;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import oleg.sopilnyak.service.model.dto.VariableItemDto;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static oleg.sopilnyak.external.controller.ControllerConstant.MODULE_BASE;
import static oleg.sopilnyak.external.controller.ControllerConstant.MODULE_PARAMETER;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class ModuleSystemControllerTest {
	private final static ObjectMapper mapper = new ObjectMapper();

	@BeforeClass
	public static void registerMapperModules(){
		mapper.registerModule(new JavaTimeModule());
	}
	private MockMvc mockMvc;

	@Mock
	private ModuleSystemFacade facade;

	@InjectMocks
	private ModuleSystemController controller = new ModuleSystemController();

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setCustomArgumentResolvers(
				)
				.build();
	}

	@After
	public void tearDown() {
		reset(facade);
	}

	@Test
	public void testRegisteredModules() throws Exception {
		String modules[] = new String[]{"module1", "module2"};
		List<String> modulesList = Arrays.asList(modules);

		when(facade.registeredModules()).thenReturn(modulesList);

		MvcResult result =
				mockMvc.perform(
						MockMvcRequestBuilders.get(MODULE_BASE)
								.contentType(APPLICATION_JSON)
				)
						.andExpect(status().isOk()).andDo(print())
						.andReturn();

		List<String> registered = mapper.readValue(result.getResponse().getContentAsString(), List.class);

		assertNotNull(registered);
		assertFalse(registered.isEmpty());
		assertEquals(2, registered.size());
		assertEquals(modulesList, registered);

		verify(facade, times(1)).registeredModules();
	}

	@Test
	public void testModuleStatus() throws Exception {
		String modulePK = "test-module";
		ModuleStatusDto status = new ModuleStatusDto();
		status.setActive(true);
		status.setCondition(ModuleHealthCondition.VERY_GOOD);
		status.setDescription("test-module-description");
		status.setModulePK(modulePK);
		when(facade.moduleStatus(modulePK)).thenReturn(status);

		MvcResult result =
				mockMvc.perform(
						MockMvcRequestBuilders.get(MODULE_BASE + "/status")
								.param(MODULE_PARAMETER, modulePK)
								.contentType(APPLICATION_JSON)
				)
						.andExpect(status().isOk()).andDo(print())
						.andReturn();

		ModuleStatusDto statusDto = mapper.readValue(result.getResponse().getContentAsString(), ModuleStatusDto.class);

		assertEquals(status, statusDto);
		verify(facade, times(1)).moduleStatus(eq(modulePK));
	}

	@Test
	public void testModuleStart() throws Exception {
		String modulePK = "test-module";
		ModuleStatusDto status = new ModuleStatusDto();
		status.setActive(true);
		status.setCondition(ModuleHealthCondition.VERY_GOOD);
		status.setDescription("test-module-description");
		status.setModulePK(modulePK);
		when(facade.moduleStart(modulePK)).thenReturn(status);

		MvcResult result =
				mockMvc.perform(
						MockMvcRequestBuilders.put(MODULE_BASE + "/start")
								.param(MODULE_PARAMETER, modulePK)
								.contentType(APPLICATION_JSON)
				)
						.andExpect(status().isOk()).andDo(print())
						.andReturn();

		ModuleStatusDto statusDto = mapper.readValue(result.getResponse().getContentAsString(), ModuleStatusDto.class);

		assertEquals(status, statusDto);
		verify(facade, times(1)).moduleStart(eq(modulePK));
	}

	@Test
	public void testModuleStop() throws Exception {
		String modulePK = "test-module";
		ModuleStatusDto status = new ModuleStatusDto();
		status.setActive(true);
		status.setCondition(ModuleHealthCondition.VERY_GOOD);
		status.setDescription("test-module-description");
		status.setModulePK(modulePK);
		when(facade.moduleStop(modulePK)).thenReturn(status);

		MvcResult result =
				mockMvc.perform(
						MockMvcRequestBuilders.put(MODULE_BASE + "/stop")
								.param(MODULE_PARAMETER, modulePK)
								.contentType(APPLICATION_JSON)
				)
						.andExpect(status().isOk()).andDo(print())
						.andReturn();

		ModuleStatusDto statusDto = mapper.readValue(result.getResponse().getContentAsString(), ModuleStatusDto.class);

		assertEquals(status, statusDto);
		verify(facade, times(1)).moduleStop(eq(modulePK));
	}

	@Test
	public void testRegisterExternalModule() throws Exception {
		ModuleStatusDto status = new ModuleStatusDto();
		status.setActive(true);
		status.setCondition(ModuleHealthCondition.VERY_GOOD);
		status.setDescription("test-module-description");

		RemoteModuleDto remote = new RemoteModuleDto();
		remote.setActive(status.isActive());
		remote.setCondition(status.getCondition());

		Map<String, VariableItemDto> configuration = new HashMap<>();
		configuration.put("test.value.1", new VariableItemDto("1", 100));
		remote.setConfiguration(configuration);
		MetricContainerDto metrics = new MetricContainerDto();

		ModuleActionDto action = createTestAction();
		ModuleMetricDto metricDto = createTestMetric(action);

		Collection<ModuleMetric> metricSet = new ArrayList<>();
		metrics.setMetrics(metricSet);
		metricSet.add(metricDto);


		remote.setMetrics(metrics);
		remote.setSystemId("test-sys");
		remote.setModuleId("test-module");
		remote.setVersionId("test-ver");
		remote.setDescription("test-desc");

		status.setModulePK(remote.primaryKey());
		when(facade.registerModule(remote)).thenReturn(status);

		MvcResult result =
				mockMvc.perform(
						MockMvcRequestBuilders.post(MODULE_BASE + "/register")
								.contentType(APPLICATION_JSON)
								.content(mapper.writeValueAsString(remote))
				)
						.andExpect(status().isOk())
						.andDo(print())
						.andReturn();

		ModuleStatusDto statusDto = mapper.readValue(result.getResponse().getContentAsString(), ModuleStatusDto.class);

		assertEquals(status, statusDto);
		verify(facade, times(1)).registerModule(eq(remote));

	}

	private ModuleMetricDto createTestMetric(ModuleActionDto action) {
		ModuleMetricDto metricDto = new ModuleMetricDto();
		metricDto.setAction(action);
		metricDto.setMeasured(Instant.now());
		metricDto.setName("test-metric");
		metricDto.setValueAsString("test-value");
		return metricDto;
	}

	private ModuleActionDto createTestAction() {
		ModuleDto module = createTestModule();
		ModuleActionDto action = new ModuleActionDto();
		action.setModule(module);
		action.setId("test-id");
		action.setDuration(100L);
		action.setHostName("test-host");
		action.setState(ModuleAction.State.PROGRESS);
		action.setStarted(Instant.now().minus(3, ChronoUnit.HOURS));
		action.setName("test-action");
		return action;
	}

	private ModuleDto createTestModule() {
		ModuleDto module = new ModuleDto();
		module.setSystemId("test-action-module-sys");
		module.setModuleId("test-action-module");
		module.setVersionId("test-action-module-ver");
		module.setDescription("test-action-module-desc");
		return module;
	}

	@Test
	public void testUnRegisterExternalModule() throws Exception {
		ModuleStatusDto status = new ModuleStatusDto();
		status.setActive(true);
		status.setCondition(ModuleHealthCondition.VERY_GOOD);
		status.setDescription("test-module-description");

		ModuleDto remote = new ModuleDto();
		remote.setSystemId("test-sys");
		remote.setModuleId("test-module");
		remote.setVersionId("test-ver");
		remote.setDescription("test-desc");

		status.setModulePK(remote.primaryKey());
		when(facade.unRegisterModule(remote)).thenReturn(status);

		MvcResult result =
				mockMvc.perform(
						MockMvcRequestBuilders.delete(MODULE_BASE + "/register")
								.contentType(APPLICATION_JSON)
								.content(mapper.writeValueAsString(remote))
				)
						.andExpect(status().isOk()).andDo(print())
						.andReturn();

		ModuleStatusDto statusDto = mapper.readValue(result.getResponse().getContentAsString(), ModuleStatusDto.class);

		assertEquals(status, statusDto);
		verify(facade, times(1)).unRegisterModule(eq(remote));

	}

	@Test
	public void testUpdateModuleState() throws Exception {
		MetricContainerDto metrics = new MetricContainerDto();

		ModuleActionDto action = createTestAction();
		ModuleMetricDto metricDto = createTestMetric(action);

		Collection<ModuleMetric> metricSet = new ArrayList<>();
		metrics.setMetrics(metricSet);
		metricSet.add(metricDto);

		ExternalModuleStateDto ping = new ExternalModuleStateDto();
		ping.setActive(true);
		ping.setCondition(ModuleHealthCondition.VERY_GOOD);
		ping.setModulePK("test=module-pk");
		ping.setMetrics(metrics);


		GeneralModuleStateDto pong = new GeneralModuleStateDto();
		pong.setActive(true);
		pong.setMainActionId(action.getId());
		pong.setModulePK(ping.getModulePK());
		pong.setCondition(ping.getCondition());
		pong.setDescription("test-desc");
		Map<String, VariableItemDto> configuration = new HashMap<>();
		configuration.put("test.value.100", new VariableItemDto("100", "One hundred."));
		pong.setConfiguration(configuration);

		when(facade.status(ping)).thenReturn(pong);

		MvcResult result =
				mockMvc.perform(
						MockMvcRequestBuilders.put(MODULE_BASE + "/ping")
								.contentType(APPLICATION_JSON)
								.content(mapper.writeValueAsString(ping))
				)
						.andExpect(status().isOk()).andDo(print())
						.andReturn();

		GeneralModuleStateDto stateDto = mapper.readValue(result.getResponse().getContentAsString(), GeneralModuleStateDto.class);
		assertEquals(pong , stateDto);

		verify(facade, times(1)).status(eq(ping));
	}

	@Test
	public void testProcessError() throws Exception {
		Exception exception = new RuntimeException("test-exception");
		when(facade.registeredModules()).thenThrow(exception);

		MvcResult result =
				mockMvc.perform(
						MockMvcRequestBuilders.get(MODULE_BASE)
								.contentType(APPLICATION_JSON)
				)
						.andExpect(status().isBadRequest())
						.andDo(print())
						.andReturn();

		String resultError = result.getResponse().getContentAsString();
		assertEquals("Error: test-exception", resultError);
	}
}