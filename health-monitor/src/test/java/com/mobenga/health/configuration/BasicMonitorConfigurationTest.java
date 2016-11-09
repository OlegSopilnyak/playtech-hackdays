package com.mobenga.health.configuration;

import com.hazelcast.core.HazelcastInstance;
import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.monitor.impl.ModuleConfigurationServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.Assert.*;

/**
 * Test for monitor configuration
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
        @ContextConfiguration(locations = {
                "classpath:com/mobenga/health/monitor/configuration/test-module-configuration-monitor.xml",
                "classpath:com/mobenga/health/monitor/storage/test-monitor-stubs.xml"
        })
        ,@ContextConfiguration(classes =
                        {
                                BasicMonitorConfiguration.class,
                                FactoryConfiguration.class,
                                DistributedConfiguration.class
                        }, loader = AnnotationConfigContextLoader.class)
})
public class BasicMonitorConfigurationTest {

    @Autowired
    private ModuleConfigurationServiceImpl configurationService;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    @Qualifier("serviceRunner")
    private ExecutorService serviceRunner;

    @Test
    public void testConfigurationServerMemoryStorage() throws Exception {
        Map<String, Map<String, ConfiguredVariableItem>> cache = configurationService.getSharedCache();
        Map<String, ConfiguredVariableItem> empty = Collections.<String, ConfiguredVariableItem>emptyMap();
        cache.put("test", empty);
        assertEquals(empty, cache.get("test"));
        assertEquals(empty, cache.remove("test"));
    }

    @Test
    public void testHazelcastInstance() throws Exception {
        assertNotNull(hazelcastInstance.getName());
        assertFalse(hazelcastInstance.getCluster().getMembers().isEmpty());
    }

    @Test
    public void testServiceRunner() throws Exception {
        AtomicBoolean value = new AtomicBoolean(false);
        Future handle = serviceRunner.submit( ()-> value.getAndSet(true));
        while (!handle.isDone());
        assertEquals(true, value.get());
    }
}