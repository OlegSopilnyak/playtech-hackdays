package com.mobenga.health.storage.impl;

import com.mobenga.health.configuration.PersistenceConfiguration;
import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.model.transport.ModuleHealthItem;
import com.mobenga.health.monitor.behavior.ModuleHealth;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.*;

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

    @Autowired
    private TimeService timer;

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

        storage.removeModule(pk);
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

        storage.removeModule(pk);
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

        storage.removeModule(pk);
        storage.removeModuleHB(pk);
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
        storage.removeModule(pk);
        storage.removeModuleHB(pk);
    }

    @Test
    public void saveActionState() throws Exception {
        final String system = "mockSysMA",
                application = "mockAppMA",
                version = "mockVerMA",
                description = "mockDescriptionMA"
                        ;

        final HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        final MonitoredAction action = storage.createMonitoredAction();
        action.setStart(timer.now());
        action.setDescription("Tests action");
        action.setState(MonitoredAction.State.INIT);

        storage.saveActionState(pk, action);

        assertEquals(MonitoredAction.State.INIT, storage.getAction(action).getState());

        action.setState(MonitoredAction.State.SUCCESS);
        action.setFinish(timer.now());
        action.setDuration(10);

        storage.saveActionState(pk, action);
        assertEquals(MonitoredAction.State.SUCCESS, storage.getAction(action).getState());

        storage.removeModule(pk);
        storage.removeAction(action);

        assertNull(storage.getAction(action));
    }

    @Test
    public void createMonitoredAction() throws Exception {
        MonitoredAction action = storage.createMonitoredAction();
        assertNotNull(action);
        assertFalse(!(action instanceof MonitoredAction));
    }

    @Test
    public void replaceConfiguration() throws Exception {
        final String system = "mockSysConf",
                application = "mockAppConf",
                version = "mockVerConf",
                description = "mockDescriptionConf"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new LocalConfiguredVariableItem("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new LocalConfiguredVariableItem("p2", "Example of parameter string", "Hello World"));

        storage.replaceConfiguration(pk, configuration);

        moduleCfg = storage.getConfiguration(key(pk));
        assertNotNull(moduleCfg.get("1.2.3.p1"));
        assertEquals(moduleCfg.get("1.2.3.p1").getType(), ConfiguredVariableItem.Type.INTEGER);
        assertNotNull(moduleCfg.get("1.2.3.p2"));
        assertEquals(moduleCfg.get("1.2.3.p2").getType(), ConfiguredVariableItem.Type.STRING);

        configuration.remove("1.2.3.p2");
        configuration.get("1.2.3.p1").set(199);
        storage.replaceConfiguration(pk, configuration);

        moduleCfg = storage.getConfiguration(key(pk));
        assertNotNull(moduleCfg.get("1.2.3.p1"));
        assertEquals(moduleCfg.get("1.2.3.p1").getType(), ConfiguredVariableItem.Type.INTEGER);
        assertEquals(moduleCfg.get("1.2.3.p1").get(Integer.class), new Integer(199));
        assertNull(moduleCfg.get("1.2.3.p2"));

        storage.removeModuleConfiguration(pk);
        storage.removeModule(pk);
    }

    @Test
    public void storeChangedConfiguration() throws Exception {
        final String system = "mockSysConf1",
                application = "mockAppConf1",
                version = "mockVerConf1",
                description = "mockDescriptionConf1"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new LocalConfiguredVariableItem("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new LocalConfiguredVariableItem("p2", "Example of parameter string", "Hello World"));

        storage.replaceConfiguration(pk, configuration);

        final Date p_date = timer.now();
        configuration.put("1.2.3.p-date", new LocalConfiguredVariableItem("p-date", "Example of parameter Date ", p_date));
        storage.storeChangedConfiguration(pk, configuration);

        moduleCfg = storage.getConfiguration(key(pk));
        assertNotNull(moduleCfg.get("1.2.3.p1"));
        assertEquals(moduleCfg.get("1.2.3.p1").getType(), ConfiguredVariableItem.Type.INTEGER);
        assertEquals(moduleCfg.get("1.2.3.p1").get(Integer.class), new Integer(150));
        assertNotNull(moduleCfg.get("1.2.3.p2"));
        assertEquals(moduleCfg.get("1.2.3.p2").getType(), ConfiguredVariableItem.Type.STRING);
        assertEquals(moduleCfg.get("1.2.3.p2").get(String.class), "Hello World");
        assertNotNull(moduleCfg.get("1.2.3.p-date"));
        assertEquals(moduleCfg.get("1.2.3.p-date").getType(), ConfiguredVariableItem.Type.TIME_STAMP);
        assertEquals(moduleCfg.get("1.2.3.p-date").get(Date.class), p_date);

        storage.removeModuleConfiguration(pk);
        storage.removeModule(pk);
    }

    @Test
    public void getApplicationsPKs() throws Exception {
        final String system = "mockSys2",
                application = "mockApp2",
                version = "mockVer2",
                description = "mockDescription2"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        String keyPK = key(storage.getModulePK(pk));

        List<String> pks = storage.getApplicationsPKs();
        assertFalse( !pks.contains(keyPK));
        HealthItemPK pk0 = pk;

        pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version+".01");
        when(pk.getDescription()).thenReturn(description);


        String key2PK = key(storage.getModulePK(pk));

        pks = storage.getApplicationsPKs();
        assertFalse( !pks.contains(keyPK));
        assertFalse( !pks.contains(key2PK));

        storage.removeModule(pk0);
        storage.removeModule(pk);
    }

    @Test
    public void getConfiguration() throws Exception {
        final String system = "mockSysConf-1",
                application = "mockAppConf-1",
                version = "mockVerConf-1",
                description = "mockDescriptionConf-1"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new LocalConfiguredVariableItem("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new LocalConfiguredVariableItem("p2", "Example of parameter string", "Hello World"));

        storage.storeChangedConfiguration(pk, configuration);

        moduleCfg = storage.getConfiguration(key(pk));

        assertEquals(2, moduleCfg.size());
        assertNotNull(moduleCfg.get("1.2.3.p1"));
        assertEquals(moduleCfg.get("1.2.3.p1").getType(), ConfiguredVariableItem.Type.INTEGER);
        assertEquals(moduleCfg.get("1.2.3.p1").get(Integer.class), new Integer(150));
        assertNotNull(moduleCfg.get("1.2.3.p2"));
        assertEquals(moduleCfg.get("1.2.3.p2").getType(), ConfiguredVariableItem.Type.STRING);
        assertEquals(moduleCfg.get("1.2.3.p2").get(String.class), "Hello World");

        when(pk.getVersionId()).thenReturn(version+"-x");
        assertEquals(0, storage.getConfiguration(key(pk)).size());

        when(pk.getVersionId()).thenReturn(version);
        storage.removeModuleConfiguration(pk);
        storage.removeModule(pk);
    }

    @Test
    public void getConfiguration1() throws Exception {
        final String system = "mockSysConf-2",
                application = "mockAppConf-2",
                version = "mockVerConf-2",
                description = "mockDescriptionConf-2"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new LocalConfiguredVariableItem("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new LocalConfiguredVariableItem("p2", "Example of parameter string", "Hello World"));

        storage.storeChangedConfiguration(pk, configuration);

        configuration.remove("1.2.3.p2");
        configuration.get("1.2.3.p1").set(199);
        storage.replaceConfiguration(pk, configuration);

        moduleCfg = storage.getConfiguration(key(pk), 1);
        assertEquals(2, moduleCfg.size());
        assertNotNull(moduleCfg.get("1.2.3.p1"));
        assertEquals(moduleCfg.get("1.2.3.p1").getType(), ConfiguredVariableItem.Type.INTEGER);
        assertEquals(moduleCfg.get("1.2.3.p1").get(Integer.class), new Integer(150));
        assertNotNull(moduleCfg.get("1.2.3.p2"));
        assertEquals(moduleCfg.get("1.2.3.p2").getType(), ConfiguredVariableItem.Type.STRING);
        assertEquals(moduleCfg.get("1.2.3.p2").get(String.class), "Hello World");

        moduleCfg = storage.getConfiguration(key(pk), 0);
        assertEquals(1, moduleCfg.size());
        assertNotNull(moduleCfg.get("1.2.3.p1"));
        assertEquals(moduleCfg.get("1.2.3.p1").getType(), ConfiguredVariableItem.Type.INTEGER);
        assertEquals(moduleCfg.get("1.2.3.p1").get(Integer.class), new Integer(199));
        assertNull(moduleCfg.get("1.2.3.p2"));

        moduleCfg = storage.getConfiguration(key(pk));
        assertEquals(1, moduleCfg.size());
        assertNotNull(moduleCfg.get("1.2.3.p1"));
        assertEquals(moduleCfg.get("1.2.3.p1").getType(), ConfiguredVariableItem.Type.INTEGER);
        assertEquals(moduleCfg.get("1.2.3.p1").get(Integer.class), new Integer(199));
        assertNull(moduleCfg.get("1.2.3.p2"));

        storage.removeModuleConfiguration(pk);
        storage.removeModule(pk);
    }

    @Test
    public void createVariableItem() throws Exception {
        final ConfiguredVariableItem item = storage.createVariableItem();
        assertNotNull(item);
        assertFalse(!(item instanceof ConfiguredVariableItem));
    }

    @Test
    public void getConfigurationVersionString() throws Exception {
        final String system = "mockSysConf-3",
                application = "mockAppConf-3",
                version = "mockVerConf-3",
                description = "mockDescriptionConf-3"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new LocalConfiguredVariableItem("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new LocalConfiguredVariableItem("p2", "Example of parameter string", "Hello World"));

        storage.storeChangedConfiguration(pk, configuration);

        assertEquals(0, storage.getConfigurationVersion(key(pk)));

        configuration.remove("1.2.3.p2");
        configuration.get("1.2.3.p1").set(199);
        storage.replaceConfiguration(pk, configuration);

        assertEquals(1, storage.getConfigurationVersion(key(pk)));
        storage.removeModuleConfiguration(pk);
        storage.removeModule(pk);
    }

    @Test
    public void getConfigurationVersionHealthPK() throws Exception {
        final String system = "mockSysConf-3",
                application = "mockAppConf-3",
                version = "mockVerConf-3",
                description = "mockDescriptionConf-3"
                        ;

        HealthItemPK pk = mock(HealthItemPK.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new LocalConfiguredVariableItem("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new LocalConfiguredVariableItem("p2", "Example of parameter string", "Hello World"));

        storage.storeChangedConfiguration(pk, configuration);

        assertEquals(0, storage.getConfigurationVersion(pk));

        configuration.remove("1.2.3.p2");
        configuration.get("1.2.3.p1").set(199);
        storage.replaceConfiguration(pk, configuration);

        assertEquals(1, storage.getConfigurationVersion(pk));
        storage.removeModuleConfiguration(pk);
        storage.removeModule(pk);
    }
}