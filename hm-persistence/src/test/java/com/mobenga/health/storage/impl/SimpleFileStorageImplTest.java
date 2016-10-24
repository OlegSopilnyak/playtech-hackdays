package com.mobenga.health.storage.impl;

import com.mobenga.health.configuration.BasicMonitorConfiguration;
import com.mobenga.health.configuration.PersistenceConfiguration;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.transport.ModuleHealthItem;
import com.mobenga.health.monitor.behavior.ModuleHealth;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static com.mobenga.health.HealthUtils.key;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The test of simple file storage for monitoring
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
        @ContextConfiguration(classes = PersistenceConfiguration.class, loader = AnnotationConfigContextLoader.class)
})

public class SimpleFileStorageImplTest {

    @Autowired
    private SimpleFileStorageImpl storage;

    @Test
    public void init() throws Exception {
        storage.init();
        assertFalse(!storage.isInitialized());
    }

    @Test
    public void getModulePK() throws Exception {
        final String system = "mockSys",
                application = "mockApp",
                version = "mockVer",
                description = "mockDescription"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        HealthItemPK stored = storage.getModulePK(pk);

        assertFalse(stored == pk);
        assertEquals(system, stored.getSystemId());
        assertEquals(application, stored.getApplicationId());
        assertEquals(version, stored.getVersionId());
        assertEquals(description, stored.getDescription());

        storage.removeEntity(pk);
    }

    @Test
    public void getModulePK1() throws Exception {
        final String system = "mockSys1",
                application = "mockApp1",
                version = "mockVer1",
                description = "mockDescription1"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);


        HealthItemPK stored = storage.getModulePK(key(pk));

        assertFalse(stored == pk);
        assertEquals(system, stored.getSystemId());
        assertEquals(application, stored.getApplicationId());
        assertEquals(version, stored.getVersionId());
        assertEquals(key(stored), stored.getDescription());

        storage.removeEntity(pk);
    }

    @Test
    public void saveHeartBeat() throws Exception {
        final String system = "mockSysHB",
                application = "mockAppHB",
                version = "mockVerHB",
                description = "mockDescriptionHB"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        ModuleHealth module = mock(ModuleHealth.class);

        when(module.getModulePK()).thenReturn(pk);
        when(module.isActive()).thenReturn(false);

        storage.saveHeartBeat(module);
        boolean saved = false;
        for(ModuleHealthItem item : storage.getSystemHealth()){
            if (item.sameAs(pk)){
                saved = true;
                assertEquals(false, item.isActive());
                break;
            }
        }
        assertFalse(!saved);


        when(module.isActive()).thenReturn(true);
        storage.saveHeartBeat(module);

        saved = false;
        for(ModuleHealthItem item : storage.getSystemHealth()){
            if (item.sameAs(pk)){
                saved = true;
                assertEquals(true, item.isActive());
                break;
            }
        }
        assertFalse(!saved);

        storage.removeEntity(pk);
    }

    @Test
    public void getSystemHealth() throws Exception {
        final String system = "mockSysHB1",
                application = "mockAppHB1",
                version = "mockVerHB1",
                description = "mockDescriptionHB1"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        ModuleHealth module = mock(ModuleHealth.class);

        when(module.getModulePK()).thenReturn(pk);
        when(module.isActive()).thenReturn(false);

        storage.saveHeartBeat(module);
        assertFalse(storage.getSystemHealth().isEmpty());

        boolean saved = false;
        for(ModuleHealthItem item : storage.getSystemHealth()){
            if (item.sameAs(pk)){
                saved = true;
                assertEquals(false, item.isActive());
                break;
            }
        }
        assertFalse(!saved);

    }

    @Test
    public void saveActionState() throws Exception {

    }

    @Test
    public void createMonitoredAction() throws Exception {

    }

    @Test
    public void replaceConfiguration() throws Exception {

    }

    @Test
    public void storeChangedConfiguration() throws Exception {

    }

    @Test
    public void getApplicationsPKs() throws Exception {

    }

    @Test
    public void getConfiguration() throws Exception {

    }

    @Test
    public void getConfiguration1() throws Exception {

    }

    @Test
    public void createVariableItem() throws Exception {

    }

}