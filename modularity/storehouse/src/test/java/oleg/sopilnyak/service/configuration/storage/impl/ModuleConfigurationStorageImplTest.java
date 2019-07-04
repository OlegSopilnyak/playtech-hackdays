/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage.impl;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.ConfigurationStorageRepository;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import oleg.sopilnyak.service.model.dto.VariableItemDto;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.anyCollectionOf;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModuleConfigurationStorageImplTest {

	@Mock
	private ModulesRegistryService registry;
	// configuration for testModule
	@Spy
	private Map<String, VariableItem> testConfiguration = new HashMap<>();

	@Spy
	private ScheduledExecutorService runner = new ScheduledThreadPoolExecutor(2);
	@Spy
	private Map<String, Map<String, VariableItem>> sharedCache = new HashMap<>();
	@Spy
	private BlockingQueue<ConfigurationStorageEvent> sharedQueue = new ArrayBlockingQueue<>(100);
	@Mock
	private ConfigurationStorageRepository repository;
	@InjectMocks
	private ModuleConfigurationStorageImpl storage = new ModuleConfigurationStorageImpl();

	@Mock
	private Module testModule;

	@Before
	public void setUp() throws Exception {
		when(registry.registered()).thenReturn(Collections.singleton(testModule));
		when(testModule.getSystemId()).thenReturn("test");
		when(testModule.getModuleId()).thenReturn("test");
		when(testModule.getVersionId()).thenReturn("test");

		testConfiguration.putIfAbsent("test.test", new VariableItemDto("test", 100));
		ModuleDto dto = new ModuleDto(testModule);
		when(repository.getConfiguration(any(ModuleDto.class))).thenReturn(testConfiguration);

		sharedCache.putIfAbsent(testModule.primaryKey(), testConfiguration);

		storage.initStorage();
		storage.listeners.clear();
	}

	@After
	public void tearDown() throws Exception {
		storage.destroyStorage();
		reset(registry, testModule);
		sharedCache.clear();
	}

	@Test
	public void initStorage() {
		final ScheduledFuture future = storage.runnerFuture;
		storage.destroyStorage();

		assertNull(storage.runnerFuture);
		assertTrue(future.isDone());

		reset(sharedCache, runner, registry);
		when(registry.registered()).thenReturn(Collections.singleton(testModule));

		sharedCache.clear();

		storage.initStorage();

		assertNotNull(storage.runnerFuture);
		assertFalse(storage.runnerFuture.isDone());

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
	public void destroyStorage() {
	}

	@Test
	public void getUpdatedVariables() {
		Map<String, VariableItem> moduleConfiguration = new HashMap<>();
		moduleConfiguration.putIfAbsent("1.1", new VariableItemDto("1", 200));
		moduleConfiguration.putIfAbsent("test.test", new VariableItemDto("test", 300));

		Map<String, VariableItem> updated = storage.getUpdatedVariables(testModule, moduleConfiguration);

		assertNotNull(updated);
		assertEquals(1, updated.size());
		Assert.assertNotEquals(moduleConfiguration.get("test.test"), updated.get("test.test"));
		Assert.assertEquals(new Integer(100), updated.get("test.test").get(Integer.class));

		verify(testConfiguration, times(1)).get(eq("1.1"));
		verify(testConfiguration, times(1)).get(eq("test.test"));
		verify(sharedQueue, times(1)).add(any(ExpandConfigurationEvent.class));
		verify(sharedCache, times(1)).put(Matchers.eq(testModule.primaryKey()), anyMap());
	}

	@Test
	public void updateConfiguration() throws InterruptedException {
		Map<String, VariableItem> moduleConfiguration = new HashMap<>();
		moduleConfiguration.putIfAbsent("1.1", new VariableItemDto("1", 200));
		moduleConfiguration.putIfAbsent("test.test", new VariableItemDto("test", 300));

		ModuleConfigurationStorage.ConfigurationListener listener = mock(ModuleConfigurationStorage.ConfigurationListener.class);
		storage.addConfigurationListener(listener);

		storage.updateConfiguration(testModule, moduleConfiguration);
		TimeUnit.MILLISECONDS.sleep(50);

		// check the results
		storage.removeConfigurationListener(listener);

		verify(sharedQueue, times(1)).add(any(ReplaceConfigurationEvent.class));
		verify(listener, times(1)).changedModules(anyCollectionOf(String.class));
	}

	@Test
	public void addConfigurationListener() {
		assertEquals(0, storage.listeners.size());
		ModuleConfigurationStorage.ConfigurationListener listener = mock(ModuleConfigurationStorage.ConfigurationListener.class);
		storage.addConfigurationListener(listener);
		assertEquals(1, storage.listeners.size());
	}

	@Test
	public void removeConfigurationListener() {
		ModuleConfigurationStorage.ConfigurationListener listener = mock(ModuleConfigurationStorage.ConfigurationListener.class);

		storage.addConfigurationListener(listener);
		assertEquals(1, storage.listeners.size());

		storage.removeConfigurationListener(listener);
		assertEquals(0, storage.listeners.size());
	}

	@Test
	public void processConfigurationEvents() {

		Module module = mock(Module.class);
		Map<String, VariableItem> configuration = new HashMap<>();

		ConfigurationStorageEvent event1 = new ExpandConfigurationEvent(module, configuration);
		ConfigurationStorageEvent event2 = new ReplaceConfigurationEvent(module, configuration);

		sharedQueue.add(event1);
		sharedQueue.add(event2);

		storage.processConfigurationEvents();

		verify(repository, times(1)).expandConfiguration(eq(event1.getModule()), eq(configuration));
		verify(repository, times(1)).replaceConfiguration(eq(event2.getModule()), eq(configuration));
	}

	@Test
	public void notifyConfigurationListeners() throws InterruptedException {
		ModuleConfigurationStorage.ConfigurationListener listener = mock(ModuleConfigurationStorage.ConfigurationListener.class);
		storage.addConfigurationListener(listener);
		Set<String> modules = new HashSet<>();
		modules.add("module-1");
		modules.add("module-2");

		storage.notifyConfigurationListeners(modules);
		TimeUnit.MILLISECONDS.sleep(100);

		storage.removeConfigurationListener(listener);

		verify(listener, times(1)).changedModules(eq(modules));
	}
}