/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.storage.simpletext.impl;

import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;

@RunWith(MockitoJUnitRunner.class)
public class SimpleTextModuleActionsRepositoryImplTest {
    
    private static final String DATA_FILE_NAME = SimpleTextModuleActionsRepositoryImpl.ACTIONS_DATA_FILE;

    @Spy
    private SimpleTextModuleActionsRepositoryImpl repository = new SimpleTextModuleActionsRepositoryImpl();
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
        reset(repository);
        new File(DATA_FILE_NAME).delete();
    }

    /**
     * Test of persist method, of class SimpleTextModuleActionsRepositoryImpl.
     */
    @Test
    public void testPersist() {
        ModuleAction action = testModuleAction();
        
        repository.persist(action);
        
        assertTrue(new File(DATA_FILE_NAME).exists());
    }

    /**
     * Test of getById method, of class SimpleTextModuleActionsRepositoryImpl.
     */
    @Test
    public void testGetById() {
        ModuleActionAdapter action1 = testModuleAction();
        action1.setId("test-1");
        ModuleActionAdapter action2 = testModuleAction();
        action2.setId("test-2");
        repository.persist(action1);
        repository.persist(action2);
        
        
        ModuleAction expResult = action1;
        ModuleAction result = repository.getById(action1.getId());
        assertEquals(expResult, result);
        
        expResult = action2;
        result = repository.getById(action2.getId());
        assertEquals(expResult, result);
    }

    /**
     * Test of findAndRestore method, of class SimpleTextModuleActionsRepositoryImpl.
     */
    @Test
    public void testFindAndRestore() throws Exception {
        ModuleActionAdapter action = testModuleAction();
        String actionId = action.getId();
        ModuleActionAdapter expResult = action;
        repository.persist(action);

        ModuleActionAdapter result = SimpleTextModuleActionsRepositoryImpl.findAndRestore(actionId);

        assertEquals(expResult, result);
    }

    /**
     * Test of transform method, of class SimpleTextModuleActionsRepositoryImpl.
     */
    @Test
    public void testTransform() {
        ModuleAction action = testModuleAction();
        String expResult = "test-id^test-name^test::module::version^null^test-host^INIT^null^0^test-description";
        String result = SimpleTextModuleActionsRepositoryImpl.transform(action);
        assertEquals(expResult, result);
    }

    /**
     * Test of postProcess method, of class SimpleTextModuleActionsRepositoryImpl.
     */
    @Test
    public void testPostProcess() throws Exception {
        ModuleActionAdapter action1 = testModuleAction();
        action1.setId("test-1");
        ModuleActionAdapter action2 = testModuleAction();
        action2.setId("test-2");
        action2.setParent(action1);

        repository.persist(action1);
        repository.persist(action2);

        ModuleAction expResult = action2;


        ModuleActionAdapter action = SimpleTextModuleActionsRepositoryImpl.findAndRestore(action2.getId());
        ModuleAction result = repository.postProcess(action);

        assertEquals(expResult, result);
    }

    /**
     * Test of restore method, of class SimpleTextModuleActionsRepositoryImpl.
     */
    @Test
    public void testRestore() throws Exception {
        ModuleActionAdapter action = testModuleAction();
        String actionData = "test-id^test-name^test::module::version^null^test-host^INIT^null^0^test-description";
        ModuleActionAdapter expResult = action;
        ModuleActionAdapter result = SimpleTextModuleActionsRepositoryImpl.restore(actionData);
        assertEquals(expResult, result);
    }

    /**
     * Test of toAction method, of class SimpleTextModuleActionsRepositoryImpl.
     */
    @Test
    public void testToAction() {
        String actionId = "test-id";
        ModuleAction expResult = ModuleActionAdapter.builder().id(actionId).build();
        ModuleAction result = SimpleTextModuleActionsRepositoryImpl.toAction(actionId);
        assertEquals(expResult, result);
    }

    /**
     * Test of loadParent method, of class SimpleTextModuleActionsRepositoryImpl.
     */
    @Test
    public void testLoadParent() throws Exception {
        ModuleActionAdapter action = testModuleAction();
        String actionId = "test-parent-id";
        action.setParent(SimpleTextModuleActionsRepositoryImpl.toAction(actionId));

        repository.persist(action);
        Map<String, ModuleActionAdapter> parents = null;
        ModuleActionAdapter expResult = null;

        ModuleActionAdapter result = repository.loadParent(actionId, parents);

        assertEquals(expResult, result);
    }

    private ModuleActionAdapter testModuleAction() {
        return ModuleActionAdapter.builder()
                .id("test-id")
                .name("test-name")
                .module(new ModuleDto("test::module:version"))
                .parent(null)
                .hostName("test-host")
                .state(ModuleAction.State.INIT)
                .started(null)
                .duration(0L)
                .description("test-description")
                .build();
    }
    
}
