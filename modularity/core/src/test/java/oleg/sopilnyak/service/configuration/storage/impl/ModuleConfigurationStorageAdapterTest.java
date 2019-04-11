/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage.impl;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.configuration.storage.event.ConfigurationStorageEvent;
import oleg.sopilnyak.service.configuration.storage.event.ExpandConfigurationEvent;
import oleg.sopilnyak.service.configuration.storage.event.ReplaceConfigurationEvent;
import oleg.sopilnyak.service.dto.ModuleDto;
import oleg.sopilnyak.service.dto.VariableItemDto;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModuleConfigurationStorageAdapterTest {

	@Mock
	private Logger logger;
	@Mock
	private ModuleConfigurationStorage.Repository repository;
	@Mock
	private ModulesRegistryService registry;
	@Mock
	private Module testModule;

	// configuration for testModule
	@Spy
	private Map<String, VariableItem> testConfiguration = new HashMap<>();

	@Spy
	private ScheduledExecutorService runner = new ScheduledThreadPoolExecutor(2);
	@Spy
	private Map<String, Map<String, VariableItem>> sharedCache = new HashMap<>();
	@Spy
	private BlockingQueue<ConfigurationStorageEvent> sharedQueue = new ArrayBlockingQueue<>(100);
	@InjectMocks
	private ModuleConfigurationStorageAdapter storage = new TestModuleConfigurationStorage();

	@Before
	public void setUp() {
		when(registry.registered()).thenReturn(Collections.singleton(testModule));
		when(testModule.getSystemId()).thenReturn("test");
		when(testModule.getModuleId()).thenReturn("test");
		when(testModule.getVersionId()).thenReturn("test");

		testConfiguration.putIfAbsent("test.test", new VariableItemDto("test", 100));
		ModuleDto dto = new ModuleDto(testModule);
		when(repository.getConfiguration(any(ModuleDto.class))).thenReturn(testConfiguration);

		sharedCache.putIfAbsent(testModule.primaryKey(), testConfiguration);

		storage.initStorage();
	}

	@After
	public void tearDown() {
		storage.destroyStorage();
		reset(registry, testModule);
		sharedCache.clear();
	}

	@Test
	public void initStorage() {
		ScheduledFuture future = (ScheduledFuture) ReflectionTestUtils.getField(storage, "runnerFuture");
		storage.destroyStorage();

		assertNull(ReflectionTestUtils.getField(storage, "runnerFuture"));

		assertTrue(future.isDone());

		reset(sharedCache, runner, registry);
		when(registry.registered()).thenReturn(Collections.singleton(testModule));

		sharedCache.clear();

		storage.initStorage();

		future = (ScheduledFuture) ReflectionTestUtils.getField(storage, "runnerFuture");

		assertNotNull(future);
		assertFalse(future.isDone());

		verify(sharedCache, times(1)).isEmpty();
		verify(runner, times(1)).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
		verify(registry, times(1)).registered();
		verify(testModule, times(2)).getSystemId();
		verify(testModule, times(2)).getModuleId();
		verify(testModule, times(2)).getVersionId();
		verify(testModule, times(2)).getDescription();
		verify(sharedCache, times(1)).putIfAbsent(eq("test::test::test"), eq(testConfiguration));
	}

	@Test
	public void getUpdatedVariables() {
		Map<String, VariableItem> moduleConfiguration = new HashMap<>();
		moduleConfiguration.putIfAbsent("1.1", new VariableItemDto("1", 200));
		moduleConfiguration.putIfAbsent("test.test", new VariableItemDto("test", 300));

		Map<String, VariableItem> updated = storage.getUpdatedVariables(testModule, moduleConfiguration);

		assertNotNull(updated);
		assertEquals(1, updated.size());
		assertNotEquals(moduleConfiguration.get("test.test"), updated.get("test.test"));
		assertEquals(new Integer(100), updated.get("test.test").get(Integer.class));

		verify(testConfiguration, times(1)).get(eq("1.1"));
		verify(testConfiguration, times(1)).get(eq("test.test"));
		verify(sharedQueue, times(1)).add(any(ExpandConfigurationEvent.class));
		verify(sharedCache,times(1)).put(eq(testModule.primaryKey()), anyMap());
	}

	@Test
	public void updateConfiguration() {
		Map<String, VariableItem> moduleConfiguration = new HashMap<>();
		moduleConfiguration.putIfAbsent("1.1", new VariableItemDto("1", 200));
		moduleConfiguration.putIfAbsent("test.test", new VariableItemDto("test", 300));

		ModuleConfigurationStorage.ConfigurationListener listener = mock(ModuleConfigurationStorage.ConfigurationListener.class);
		storage.addConfigurationListener(listener);

		storage.updateConfiguration(testModule, moduleConfiguration);
		storage.removeConfigurationListener(listener);

		verify(sharedQueue, times(1)).add(any(ReplaceConfigurationEvent.class));
		verify(listener, times(1)).changedModules(anyCollectionOf(String.class));
	}

	@Test
	public void processConfigurationEvents() {
		// TODO cover storage events processing
	}

	@Test
	public void notifyConfigurationListeners() {
		ModuleConfigurationStorage.ConfigurationListener listener = mock(ModuleConfigurationStorage.ConfigurationListener.class);
		storage.addConfigurationListener(listener);
		Set<String> modules = new HashSet<>();
		modules.add("module-1");
		modules.add("module-2");

		storage.notifyConfigurationListeners(modules);

		storage.removeConfigurationListener(listener);
		verify(listener, times(1)).changedModules(eq(modules));
	}

	// inner classes
	private static class TestModuleConfigurationStorage extends ModuleConfigurationStorageAdapter{
		@Autowired
		private Logger logger;
		@Autowired
		private Repository repository;
		@Override
		protected Logger getLogger() {
			return logger;
		}

		@Override
		public Repository getConfigurationRepository() {
			return repository;
		}
	}
}