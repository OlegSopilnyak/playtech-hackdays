package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.ModulePK;
import com.mobenga.health.model.transport.ModuleWrapper;
import com.mobenga.health.monitor.DistributedContainersService;
import com.mobenga.health.monitor.HealthModuleService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.storage.ConfigurationStorage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;

import static org.junit.Assert.assertEquals;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test for ModuleConfigurationServiceImpl
 *
 * @see ModuleConfigurationServiceImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class ModuleConfigurationServiceImplTest {

    @InjectMocks
    private final ModuleConfigurationServiceImpl service = new ModuleConfigurationServiceImpl();

    @Mock
    private ConfigurationStorage storage;
    @Mock
    private DistributedContainersService distributed;
    @Mock
    private ModuleStateNotificationService notifier;
    @Mock
    private HealthModuleService modules;
    @Spy
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    private final ModuleWrapper module = new ModuleWrapper();

    @Before
    public void initCache() throws Exception {
        module.setSystemId("sys");
        module.setApplicationId("app");
        module.setVersionId("test");
        module.setDescription("Test module");

        Map<String, ConfiguredVariableItem> moduleCache = new LinkedHashMap<>();
        {
            ConfiguredVariableItem item = createItem("name1");
            putToCache("1.2.3", item, moduleCache);
        }
        {
            ConfiguredVariableItem item = createItem("name2");
            putToCache("1.2.3", item, moduleCache);
        }
        {
            ConfiguredVariableItem item = createItem("name3");
            putToCache("1.2.3.4", item, moduleCache);
        }
        when(storage.replaceConfiguration(eq(module), any(Map.class))).then(new Answer<Map>() {
            @Override
            public Map answer(InvocationOnMock invocationOnMock) throws Throwable {
                return (Map) invocationOnMock.getArguments()[1];
            }
        });
        when(modules.getModule(any(ModulePK.class))).thenReturn(module);
        when(distributed.queue(anyString())).thenReturn(new ArrayBlockingQueue(10));
        service.initialize();

        service.changeConfiguration(module, moduleCache);
        synchronized (module) {
            module.wait(300);
        }
        verify(storage, times(1)).replaceConfiguration(eq(module), any(Map.class));
    }

    @After
    public void destroyCache() {
        service.shutdown();
        reset(storage);
    }

    @Test
    public void testGetConfigurationGroup() throws Exception {
        Map config = service.getConfigurationGroup(module, "1.2.3");
        assertEquals(3, config.size());
        config = service.getConfigurationGroup(module, "1.2.3.4");
        assertEquals(1, config.size());
    }

    @Test
    public void testGetUpdatedVariables() throws Exception {

        Map<String, ConfiguredVariableItem> moduleCache = new LinkedHashMap<>();
        {
            ConfiguredVariableItem item = createItem("name1");
            when(item.getValue()).thenReturn("3");
            putToCache("1.2.3", item, moduleCache);
        }
        {
            ConfiguredVariableItem item = createItem("name2");
            when(item.getValue()).thenReturn("4");
            putToCache("1.2.3", item, moduleCache);
        }
        {
            ConfiguredVariableItem item = createItem("name3");
            when(item.getValue()).thenReturn("5");
            putToCache("1.2.3", item, moduleCache);
        }

        Map<String, ConfiguredVariableItem> currentModuleCache = service.getUpdatedVariables(module, moduleCache);
        synchronized (module) {
            module.wait(300);
        }
        assertEquals(2, (currentModuleCache).size());
        assertEquals("1", currentModuleCache.get("1.2.3.name1").getValue());
        assertEquals("1", currentModuleCache.get("1.2.3.name2").getValue());
        assertEquals(4, (currentModuleCache = service.getConfigurationGroup(module, "1.2.3")).size());
        assertEquals("5", currentModuleCache.get("1.2.3.name3").getValue());
        verify(storage, times(1)).storeChangedConfiguration(eq(module), any(Map.class));
    }

    @Test
    public void testUpdatedVariables() throws Exception {
        reset(storage);
        Map<String, ConfiguredVariableItem> moduleCache = new LinkedHashMap<>();
        {
            ConfiguredVariableItem item = createItem("name1");
            when(item.getValue()).thenReturn("3");
            putToCache("1.2.3", item, moduleCache);
        }
        {
            ConfiguredVariableItem item = createItem("name2");
            when(item.getValue()).thenReturn("4");
            putToCache("1.2.3", item, moduleCache);
        }
        {
            ConfiguredVariableItem item = createItem("name3");
            when(item.getValue()).thenReturn("5");
            putToCache("1.2.3", item, moduleCache);
        }
        when(storage.replaceConfiguration(eq(module), any(Map.class))).then(new Answer<Map>() {
            @Override
            public Map answer(InvocationOnMock invocationOnMock) throws Throwable {
                return (Map) invocationOnMock.getArguments()[1];
            }
        });

        Map<String, ConfiguredVariableItem> currentModuleCache = service.changeConfiguration(module, moduleCache);

        assertEquals(3, (currentModuleCache).size());
        assertEquals("3", currentModuleCache.get("1.2.3.name1").getValue());
        assertEquals("4", currentModuleCache.get("1.2.3.name2").getValue());
        synchronized (module) {
            module.wait(300);
        }
        verify(storage, times(1)).replaceConfiguration(eq(module), any(Map.class));
        assertEquals(3, (currentModuleCache = service.getConfigurationGroup(module, "1.2.3")).size());
        assertEquals("5", currentModuleCache.get("1.2.3.name3").getValue());
    }

    // private method
    private ConfiguredVariableItem createItem(String name) {
        ConfiguredVariableItem item = mock(ConfiguredVariableItem.class);
        when(item.getName()).thenReturn(name);
        when(item.getValue()).thenReturn("1");
        when(item.getType()).thenReturn(ConfiguredVariableItem.Type.STRING);
        return item;
    }

    private void putToCache(String group, ConfiguredVariableItem item, Map<String, ConfiguredVariableItem> cache) {
        cache.put(group + "." + item.getName(), item);
    }
}
