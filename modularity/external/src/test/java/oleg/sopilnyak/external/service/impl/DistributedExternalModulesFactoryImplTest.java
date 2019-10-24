/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service.impl;

import oleg.sopilnyak.external.dto.MetricContainerDto;
import oleg.sopilnyak.module.ModuleBasics;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DistributedExternalModulesFactoryImplTest {

	@Spy
	private Map<String, ExternalModuleImpl> sharedMap = new HashMap<>();

	@InjectMocks
	private DistributedExternalModulesFactoryImpl factory = spy(new DistributedExternalModulesFactoryImpl());

	@After
	public void tearDown() throws Exception {
		sharedMap.clear();
		reset(sharedMap);
	}

	@Test
	public void testUpdateModule() {
		ExternalModuleImpl module = spy(testModule());

		factory.updateModule(module);

		verify(module, times(1)).primaryKey();
		verify(sharedMap, times(1)).put(eq(module.primaryKey()), eq(module));
		assertEquals(module, sharedMap.get(module.primaryKey()));
	}

	@Test
	public void testRetrieveModule() {
		ExternalModuleImpl module = spy(testModule());
		String modulePK = module.primaryKey();

		assertNull(factory.retrieveModule(modulePK));
		factory.updateModule(module);

		reset(module, sharedMap);

		assertEquals(module, factory.retrieveModule(modulePK));
		verify(sharedMap, times(1)).get(eq(modulePK));
	}

	@Test
	public void testRemoveModule() {
		ExternalModuleImpl module = spy(testModule());
		String modulePK = module.primaryKey();
		assertNull(factory.retrieveModule(modulePK));
		factory.updateModule(module);
		assertEquals(module, factory.retrieveModule(modulePK));

		reset(module, sharedMap);

		factory.removeModule(module);

		verify(module, times(1)).primaryKey();
		verify(sharedMap, times(1)).remove(eq(modulePK));

		assertNull(factory.retrieveModule(module.primaryKey()));

	}

	@Test
	public void testRetrieveModuleBy() {
		ExternalModuleImpl module = spy(testModule());
		factory.updateModule(module);

		assertEquals(module, factory.retrieveModuleBy(module));
		verify(module, times(2)).primaryKey();
		verify(sharedMap, times(1)).computeIfAbsent(eq(module.primaryKey()), any(Function.class));
		reset(sharedMap);

		ModuleBasics pattern = mock(ModuleBasics.class);
		when(pattern.getSystemId()).thenReturn("mock-system");
		when(pattern.getModuleId()).thenReturn("mock-module");
		when(pattern.getVersionId()).thenReturn("mock-version");
		when(pattern.getDescription()).thenReturn("mock-description");
		when(pattern.primaryKey()).thenReturn("mock-module-pk");

		ExternalModuleImpl moduleByPattern = factory.retrieveModuleBy(pattern);
		verify(pattern, times(1)).primaryKey();
		verify(pattern, times(1)).getSystemId();
		verify(pattern, times(1)).getModuleId();
		verify(pattern, times(1)).getVersionId();
		verify(pattern, times(1)).getDescription();
		verify(sharedMap, times(1)).computeIfAbsent(eq("mock-module-pk"), any(Function.class));
		verify(factory, times(1)).createExternalModule(eq(pattern));

		assertEquals("mock-system", moduleByPattern.getSystemId());
		assertEquals("mock-module", moduleByPattern.getModuleId());
		assertEquals("mock-version", moduleByPattern.getVersionId());
		assertEquals("mock-description", moduleByPattern.getDescription());
		assertNotEquals("mock-module-pk", moduleByPattern.primaryKey());
		assertEquals(moduleByPattern, factory.retrieveModule("mock-module-pk"));
	}

	@Test
	public void testCreateExternalModule() {
		ModuleBasics pattern = mock(ModuleBasics.class);
		when(pattern.getSystemId()).thenReturn("mock-system");
		when(pattern.getModuleId()).thenReturn("mock-module");
		when(pattern.getVersionId()).thenReturn("mock-version");
		when(pattern.getDescription()).thenReturn("mock-description");
		when(pattern.primaryKey()).thenReturn("mock-module-pk");

		ExternalModuleImpl moduleByPattern = factory.createExternalModule(pattern);

		verify(pattern, times(1)).getSystemId();
		verify(pattern, times(1)).getModuleId();
		verify(pattern, times(1)).getVersionId();
		verify(pattern, times(1)).getDescription();
		assertEquals("mock-system", moduleByPattern.getSystemId());
		assertEquals("mock-module", moduleByPattern.getModuleId());
		assertEquals("mock-version", moduleByPattern.getVersionId());
		assertEquals("mock-description", moduleByPattern.getDescription());
		assertNotEquals("mock-module-pk", moduleByPattern.primaryKey());
	}

	// private methods
	private ExternalModuleImpl testModule() {
		final ExternalModuleImpl module = new ExternalModuleImpl();
		module.setSystemId("test-system");
		module.setModuleId("test-module");
		module.setVersionId("test-version-0");
		module.setDescription("Test description");
		module.setModuleValues(new LinkedHashMap<>());
		module.setMetricsContainer(new MetricContainerDto());
		return module;
	}

}