package com.mobenga.health.storage.impl;

import com.mobenga.health.configuration.PersistenceConfiguration;
import com.mobenga.health.model.MonitoredActionEntity;
import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.model.business.ModuleHealth;
import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.MonitoredAction;
import com.mobenga.health.model.business.out.ModuleOutputCriteriaBase;
import com.mobenga.health.model.business.out.ModuleOutputMessage;
import com.mobenga.health.model.business.out.log.ModuleLoggerMessage;
import com.mobenga.health.model.persistence.ValidatingEntity;
import com.mobenga.health.model.transport.ConfiguredVariableItemDto;
import com.mobenga.health.monitor.TimeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        ModuleKey stored = storage.getModulePK(pk);

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

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);


        ModuleKey stored = storage.getModulePK(key(pk));

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

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        ModuleHealth module = mock(ModuleHealth.class);

        // TODO realize the test
//        when(module).thenReturn(pk);
        when(module.isActive()).thenReturn(false);

        storage.saveHeartBeat(module);
        boolean saved = false;
        for(ModuleHealth item : storage.getSystemHealth()){
            // TODO realize the test
//            if (item.sameAs(pk)){
                saved = true;
//                assertEquals(false, item.isActive());
//                break;
//            }
        }
        assertFalse(!saved);


        when(module.isActive()).thenReturn(true);
        storage.saveHeartBeat(module);

        saved = false;
//        for(ModuleHealthItem item : storage.getSystemHealth()){
//            if (item.sameAs(pk)){
                saved = true;
//                assertEquals(true, item.isActive());
//                break;
//            }
//        }
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

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);
        ModuleHealth module = mock(ModuleHealth.class);

//        when(module.getModulePK()).thenReturn(pk);
        when(module.isActive()).thenReturn(false);

        storage.saveHeartBeat(module);
        assertFalse(storage.getSystemHealth().isEmpty());

        boolean saved = false;
//        for(ModuleHealthItem item : storage.getSystemHealth()){
//            if (item.sameAs(pk)){
                saved = true;
//                assertEquals(false, item.isActive());
//                break;
//            }
//        }
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

        final ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        final MonitoredActionEntity action = (MonitoredActionEntity) storage.createMonitoredAction();
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

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new ConfiguredVariableItemDto("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new ConfiguredVariableItemDto("p2", "Example of parameter string", "Hello World"));

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

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new ConfiguredVariableItemDto("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new ConfiguredVariableItemDto("p2", "Example of parameter string", "Hello World"));

        storage.replaceConfiguration(pk, configuration);

        final Date p_date = timer.now();
        configuration.put("1.2.3.p-date", new ConfiguredVariableItemDto("p-date", "Example of parameter Date ", p_date));
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

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        String keyPK = key(storage.getModulePK(pk));

        List<String> pks = storage.getApplicationsPKs();
        assertFalse( !pks.contains(keyPK));
        ModuleKey pk0 = pk;

        pk = mock(ModuleKey.class);
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

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new ConfiguredVariableItemDto("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new ConfiguredVariableItemDto("p2", "Example of parameter string", "Hello World"));

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

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new ConfiguredVariableItemDto("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new ConfiguredVariableItemDto("p2", "Example of parameter string", "Hello World"));

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

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new ConfiguredVariableItemDto("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new ConfiguredVariableItemDto("p2", "Example of parameter string", "Hello World"));

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

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        Map<String, ConfiguredVariableItem> configuration = new HashMap<>(), moduleCfg;
        configuration.put("1.2.3.p1", new ConfiguredVariableItemDto("p1", "Example of parameter number", 150));
        configuration.put("1.2.3.p2", new ConfiguredVariableItemDto("p2", "Example of parameter string", "Hello World"));

        storage.storeChangedConfiguration(pk, configuration);

        assertEquals(0, storage.getConfigurationVersion(pk));

        configuration.remove("1.2.3.p2");
        configuration.get("1.2.3.p1").set(199);
        storage.replaceConfiguration(pk, configuration);

        assertEquals(1, storage.getConfigurationVersion(pk));
        storage.removeModuleConfiguration(pk);
        storage.removeModule(pk);
    }

    @Test
    public void testCreateModuleOutput() throws Exception {
        final String system = "mockSysConf-mo1",
                application = "mockAppConf-mo1",
                version = "mockVerConf-mo1",
                description = "mockDescriptionConf-mo1"
                        ;

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        ModuleOutputMessage message = storage.createModuleOutput(pk, ModuleLoggerMessage.LOG_OUTPUT_TYPE);
        assertNotNull(message);
        assertNull(message.getId());
        message = storage.createModuleOutput(pk, "not-exists");
        assertNull(message);
    }

    @Test
    public void testSaveModuleOutput() throws Exception {
        final String system = "mockSysConf-mo1",
                application = "mockAppConf-mo1",
                version = "mockVerConf-mo1",
                description = "mockDescriptionConf-mo1"
                        ;

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        ModuleOutputMessage output = storage.createModuleOutput(pk, ModuleLoggerMessage.LOG_OUTPUT_TYPE);
        assertNotNull(output);
        assertNull(output.getId());
        assertFalse(!(output instanceof ModuleLoggerMessage));
        ModuleLoggerMessage message = (ModuleLoggerMessage) output;
        message.setWhenOccured(timer.now());
        message.setModulePK(key(pk));
        message.setPayload("Hello world");
        storage.saveModuleOutput(output);
        assertNotNull(output.getId());


        ModuleOutputCriteriaBase criteriaBase = new ModuleOutputCriteriaBase();
        criteriaBase.setOutputId(output.getId());
        storage.delete(criteriaBase);
    }

    @Test(expected = ValidatingEntity.EntityInvalidState.class)
    public void testSaveModuleOutputFail() throws Exception {
        final String system = "mockSysConf-mo1",
                application = "mockAppConf-mo1",
                version = "mockVerConf-mo1",
                description = "mockDescriptionConf-mo1"
                        ;

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        ModuleOutputMessage output = storage.createModuleOutput(pk, ModuleLoggerMessage.LOG_OUTPUT_TYPE);
        assertNotNull(output);
        assertNull(output.getId());
        assertFalse(!(output instanceof ModuleLoggerMessage));
        ModuleLoggerMessage message = (ModuleLoggerMessage) output;
        message.setWhenOccured(timer.now());
        message.setModulePK(key(pk));
        storage.saveModuleOutput(output);
        fail("Here exception should be thrown.");
    }

    @Test
    public void testSaveModuleOutputWithAction() throws Exception {
        final String system = "mockSysConf-mo2",
                application = "mockAppConf-mo2",
                version = "mockVerConf-mo2",
                description = "mockDescriptionConf-mo1"
                        ;

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        MonitoredActionEntity action = (MonitoredActionEntity) storage.createMonitoredAction();
        action.setStart(timer.now());
        action.setDescription("Just action");
        action.setHost("localhost");
        action.setState(MonitoredAction.State.INIT);
        storage.saveActionState(pk, action);

        ModuleOutputMessage output = storage.createModuleOutput(pk, ModuleLoggerMessage.LOG_OUTPUT_TYPE);
        assertNotNull(output);
        assertNull(output.getId());
        assertFalse(!(output instanceof ModuleLoggerMessage));
        ModuleLoggerMessage message = (ModuleLoggerMessage) output;
        message.setActionId(action.getId());
        message.setWhenOccured(timer.now());
        message.setModulePK(key(pk));
        message.setPayload("Hello world");
        storage.saveModuleOutput(output);
        assertNotNull(output.getId());

        for(int i=1; i < 10;i++) {
            output = storage.createModuleOutput(pk, ModuleLoggerMessage.LOG_OUTPUT_TYPE);
            message = (ModuleLoggerMessage) output;
            message.setActionId(action.getId());
            message.setWhenOccured(timer.now());
            message.setModulePK(key(pk));
            message.setPayload("Hello world"+i);
            storage.saveModuleOutput(output);
            assertNotNull(output.getId());
        }

        ModuleOutputCriteriaBase criteriaBase = new ModuleOutputCriteriaBase();
        criteriaBase.setActionIds(new String[]{action.getId()});
        storage.delete(criteriaBase);
        storage.removeAction(action);
        storage.removeModule(pk);
    }

    @Test
    public void testSelectModuleOutput() throws Exception {
        final String system = "mockSysConf-mo3",
                application = "mockAppConf-mo3",
                version = "mockVerConf-mo3",
                description = "mockDescriptionConf-mo3"
                        ;

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        MonitoredActionEntity action = (MonitoredActionEntity) storage.createMonitoredAction();
        action.setStart(timer.now());
        action.setDescription("Just action");
        action.setHost("localhost");
        action.setState(MonitoredAction.State.INIT);
        storage.saveActionState(pk, action);
        action.setState(MonitoredAction.State.PROGRESS);
        storage.saveActionState(pk, action);

        ModuleOutputMessage output = storage.createModuleOutput(pk, ModuleLoggerMessage.LOG_OUTPUT_TYPE);
        assertNotNull(output);
        assertNull(output.getId());
        assertFalse(!(output instanceof ModuleLoggerMessage));
        ModuleLoggerMessage message = (ModuleLoggerMessage) output;
        message.setActionId(action.getId());
        message.setWhenOccured(timer.now());
        message.setModulePK(key(pk));
        message.setPayload("Hello world");
        storage.saveModuleOutput(output);
        assertNotNull(output.getId());

        for(int i=1; i < 100;i++) {
            output = storage.createModuleOutput(pk, ModuleLoggerMessage.LOG_OUTPUT_TYPE);
            message = (ModuleLoggerMessage) output;
            message.setActionId(action.getId());
            message.setWhenOccured(timer.now());
            message.setModulePK(key(pk));
            message.setPayload("Hello world"+i);
            storage.saveModuleOutput(output);
            assertNotNull(output.getId());
        }

        ModuleOutputCriteriaBase criteriaBase = new ModuleOutputCriteriaBase();
        criteriaBase.setActionIds(new String[]{action.getId()});
        Pageable pager = new PageRequest(0, 10);
        Page<ModuleOutputMessage>page = storage.select(criteriaBase, pager);
        assertEquals(100, page.getTotalElements());
        assertEquals(10, page.getSize());
        assertEquals(10, page.getTotalPages());

        storage.delete(criteriaBase);
        storage.removeAction(action);
        storage.removeModule(pk);
    }
    @Test
    public void testRemoveModuleOutput() throws Exception {
        final String system = "mockSysConf-mo3",
                application = "mockAppConf-mo3",
                version = "mockVerConf-mo3",
                description = "mockDescriptionConf-mo3";

        ModuleKey pk = mock(ModuleKey.class);
        when(pk.getSystemId()).thenReturn(system);
        when(pk.getApplicationId()).thenReturn(application);
        when(pk.getVersionId()).thenReturn(version);
        when(pk.getDescription()).thenReturn(description);

        MonitoredActionEntity action = (MonitoredActionEntity) storage.createMonitoredAction();
        action.setStart(timer.now());
        action.setDescription("Just action");
        action.setHost("localhost");
        action.setState(MonitoredAction.State.INIT);
        storage.saveActionState(pk, action);
        action.setState(MonitoredAction.State.PROGRESS);
        storage.saveActionState(pk, action);
        ModuleOutputMessage output = storage.createModuleOutput(pk, ModuleLoggerMessage.LOG_OUTPUT_TYPE);
        assertNotNull(output);
        assertNull(output.getId());
        assertFalse(!(output instanceof ModuleLoggerMessage));
        ModuleLoggerMessage message = (ModuleLoggerMessage) output;
        message.setActionId(action.getId());
        message.setWhenOccured(timer.now());
        message.setModulePK(key(pk));
        message.setPayload("Hello world");
        storage.saveModuleOutput(output);
        assertNotNull(output.getId());

        for(int i=1; i < 100;i++) {
            output = storage.createModuleOutput(pk, ModuleLoggerMessage.LOG_OUTPUT_TYPE);
            message = (ModuleLoggerMessage) output;
            message.setActionId(action.getId());
            message.setWhenOccured(timer.now());
            message.setModulePK(key(pk));
            message.setPayload("Hello world"+i);
            storage.saveModuleOutput(output);
            assertNotNull(output.getId());
        }

        final ModuleOutputCriteriaBase criteriaBase = new ModuleOutputCriteriaBase();
        criteriaBase.setActionIds(new String[]{action.getId()});
        Pageable pager = new PageRequest(0, 10);

        Page<ModuleOutputMessage>page = storage.select(criteriaBase, pager);
        assertEquals(100, page.getTotalElements());

        storage.delete(criteriaBase);
        page = storage.select(criteriaBase, pager);
        assertEquals(0, page.getTotalElements());

        storage.removeAction(action);
        storage.removeModule(pk);
    }
}