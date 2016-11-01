package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.storage.ConfigurationStorage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.mobenga.health.HealthUtils.key;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for ModuleConfigurationServiceImpl
 * @see ModuleConfigurationServiceImpl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:com/mobenga/health/monitor/impl/test-module-configuration.xml"})
public class ModuleConfigurationServiceImplTest {

    @Autowired
    private ModuleConfigurationServiceImpl service;

    @Autowired
    @Qualifier("sharedCacheMap")
    private Map<String, Map<String,ConfiguredVariableItem>> cache;

    @Autowired
    private ConfigurationStorage storage;

    @Autowired
    private HealthItemPK module;

    @Before
    public void initCache(){
        when(module.getSystemId()).thenReturn("sys");
        when(module.getApplicationId()).thenReturn("app");
        when(module.getVersionId()).thenReturn("test");

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
        cache.put(key(module), moduleCache);
        service.setSharedCache(cache);
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

    }

    @Test
    public void testUpdatedVariables() throws Exception {

    }
    // private method
    private ConfiguredVariableItem createItem(String name){
        ConfiguredVariableItem item = mock(ConfiguredVariableItem.class);
        when(item.getName()).thenReturn(name);
        when(item.getValue()).thenReturn("1");
        when(item.getType()).thenReturn(ConfiguredVariableItem.Type.STRING);
        return item;
    }
    private void putToCache(String group, ConfiguredVariableItem item, Map<String, ConfiguredVariableItem> cache){
        cache.put(group+"."+item.getName(), item);
    }
}