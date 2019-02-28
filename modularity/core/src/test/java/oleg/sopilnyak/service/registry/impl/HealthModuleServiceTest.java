package oleg.sopilnyak.service.registry.impl;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.module.model.action.ResultModuleAction;
import oleg.sopilnyak.module.model.action.SuccessModuleAction;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HealthModuleServiceTest {

    @Mock
    private Module module;
    @Spy
    private ModuleActionAdapter mainAction;
    @Mock
    private MetricsContainer metricsContainer;
    @Mock
    private ActionMetricsContainer actionMetricsContainer;
    @Mock
    private ModuleActionFactory actionsFactory;
    @InjectMocks
    private HealthModuleService service = new HealthModuleService();

    @Before
    public void setUp() throws Exception {
        when(module.getSystemId()).thenReturn("sys-test");
        when(module.getModuleId()).thenReturn("mod-test");
        when(module.getVersionId()).thenReturn("ver-test");
        when(module.getDescription()).thenReturn("desc-test");
        when(module.primaryKey()).thenReturn("test-pk");
        mainAction = new ModuleActionAdapter(module, "test");
        when(actionsFactory.createModuleMainAction(any(Module.class))).thenReturn(mainAction);
        ResultModuleAction result = new SuccessModuleAction(mainAction);
        when(actionsFactory.executeAtomicModuleAction(any(Module.class), anyString(), any(Runnable.class), anyBoolean())).thenReturn(result);
        when(metricsContainer.action()).thenReturn(actionMetricsContainer);

        service.moduleStart();
    }

    @After
    public void tearDown() throws Exception {
        service.moduleStop();
    }

    @Test
    public void testAdd() {
        service.add(module);
    }

    @Test
    public void testRemove() {
    }

    @Test
    public void testRegistered() {
    }

    @Test
    public void testGetRegistered() {
    }

    @Test
    public void testGetRegistered1() {
    }

    @Test
    public void testInitAsService() {
    }

    @Test
    public void testShutdownAsService() {
    }

    @Test
    public void testConfigurationItemChanged() {
    }

    @Test
    public void testInspectModule() {
    }

    @Test
    public void testScanModulesHealth() {
    }

    @Test
    public void store() {
    }
}