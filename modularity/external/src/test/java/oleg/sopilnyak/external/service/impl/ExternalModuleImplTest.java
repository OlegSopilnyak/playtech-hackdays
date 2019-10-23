/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service.impl;

import oleg.sopilnyak.external.dto.ModuleValuesDto;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ExternalModuleImplTest {

	private ExternalModuleImpl module = new ExternalModuleImpl();

	@After
	public void tearDown() throws Exception {
		module.moduleStop();
		module.setTouched(-1L);
		module.clearValues();
		module.setModuleChecker(null);
	}

	@Test
	public void testModuleStart() {
		module.moduleStart();
		assertTrue(module.isActive());
	}

	@Test
	public void testModuleStop() {
		module.moduleStart();
		assertTrue(module.isActive());
		module.moduleStop();
		assertFalse(module.isActive());
	}

	@Test
	public void testCanRestart() {
		assertTrue(module.canRestart());
	}

	@Test
	public void testValues() {
		assertFalse(module.hasValues());
		ModuleValuesDto valuesDto = new ModuleValuesDto();
		valuesDto.setHost("host");
		module.registerValues(valuesDto);
		assertTrue(module.hasValues());
		assertEquals(valuesDto, module.values().findFirst().get());
		assertEquals(1L, module.values().count());
	}

	@Test
	public void testIsModuleRegistered() {
		assertFalse(module.isModuleRegistered());
		ExternalModuleChecker checker = mock(ExternalModuleChecker.class);
		module.setModuleChecker(checker);
		assertFalse(module.isModuleRegistered());
		verify(checker, times(1)).isValidModule(eq(module));
		reset(checker);
		when(checker.isValidModule(module)).thenReturn(true);
		assertTrue(module.isModuleRegistered());
		verify(checker, times(1)).isValidModule(eq(module));
	}

	@Test
	public void testValuesFor() {
		assertFalse(module.hasValues());
		ModuleValuesDto valuesDto = new ModuleValuesDto();
		valuesDto.setHost("host");
		module.registerValues(valuesDto);
		assertTrue(module.hasValues());

		assertEquals(valuesDto, module.valuesFor("host"));
	}

	@Test
	public void testIsExpired() throws InterruptedException {
		ModuleValuesDto valuesDto = new ModuleValuesDto();
		valuesDto.setHost("host");
		module.registerValues(valuesDto);
		long expired = TimeUnit.MILLISECONDS.toMillis(200);
		module.moduleStart();
		assertFalse(module.isExpired(expired));
		TimeUnit.MILLISECONDS.sleep(300);
		assertTrue(module.isExpired(expired));
	}

	@Test
	public void testHasValues() {
		assertFalse(module.hasValues());
		ModuleValuesDto valuesDto = new ModuleValuesDto();
		valuesDto.setHost("host");
		module.registerValues(valuesDto);
		assertTrue(module.hasValues());
	}

	@Test
	public void testRegistryIn() {
		String host = "host";
		module.registryIn(host);
		assertEquals(host, module.registryIn());
	}


	@Test
	public void registerValues() {
		ModuleValuesDto valuesDto = new ModuleValuesDto();
		valuesDto.setHost("host");
		module.registerValues(valuesDto);
		assertTrue(module.hasValues());
		assertEquals(valuesDto, module.valuesFor(valuesDto.getHost()));
	}

	@Test
	public void unRegisterValues() {
		ModuleValuesDto valuesDto = new ModuleValuesDto();
		valuesDto.setHost("host");
		module.registerValues(valuesDto);
		assertTrue(module.hasValues());

		module.unRegisterValues(valuesDto);
		assertFalse(module.hasValues());
		assertNull(module.valuesFor(valuesDto.getHost()));
	}
}