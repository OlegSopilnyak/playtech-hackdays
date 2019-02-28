package oleg.sopilnyak.service.registry.impl;

import oleg.sopilnyak.configuration.ModuleSystemConfiguration;
import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.DurationMetricsContainer;
import oleg.sopilnyak.module.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.module.model.action.ResultModuleAction;
import oleg.sopilnyak.module.model.action.SuccessModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.action.impl.ModuleActionFactoryImpl;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.dto.VariableItemDto;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static oleg.sopilnyak.service.registry.ModulesRegistryService.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

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
    private DurationMetricsContainer durationMetricsContainer;
    @Mock
    private HeartBeatMetricContainer heartBeatMetricContainer;
    @Spy
    protected ScheduledExecutorService activityRunner = new ModuleUtilityConfiguration().getScheduledExecutorService();
    @Spy
    private TimeService timeService = new ModuleUtilityConfiguration().getTimeService();
    @Spy
    private UniqueIdGenerator idGenerator = new ModuleUtilityConfiguration().getUniqueIdGenerator();
    @Spy
    private ModuleActionFactory actionsFactory = new ModuleSystemConfiguration().getModuleActionFactory();
    @Mock
    private ModuleConfigurationStorage configurationStorage;
    @Mock
    private ModuleMetricStorage metricStorage;
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
        ResultModuleAction result = new SuccessModuleAction(mainAction);
        when(metricsContainer.action()).thenReturn(actionMetricsContainer);
        when(metricsContainer.duration()).thenReturn(durationMetricsContainer);
        when(metricsContainer.health()).thenReturn(heartBeatMetricContainer);

        ((ModuleActionFactoryImpl)actionsFactory).setUp();
        mainAction.setHostName((String) ReflectionTestUtils.getField(actionsFactory, "hostName"));
        ReflectionTestUtils.setField(actionsFactory, "idGenerator", idGenerator);

        service.moduleStart();
    }

    @After
    public void tearDown() throws Exception {
        service.moduleStop();
        reset(module, actionsFactory, metricsContainer, actionMetricsContainer);
    }

    @Test
    public void testAdd() {
        service.add(module);

        Map<String, Module> modules = (Map<String, Module>) ReflectionTestUtils.getField(service, "modules");
        assertEquals(module, modules.get(module.primaryKey()));
        verify(actionsFactory, times(2)).startMainAction(eq(service));
    }

    @Test(expected = AssertionError.class)
    public void testAddFail() {
        service.add(null);
        fail("Here I'm waiting for an exception.");
    }

    @Test
    public void testRemove() {
        service.add(module);

        Map<String, Module> modules = (Map<String, Module>) ReflectionTestUtils.getField(service, "modules");
        assertEquals(module, modules.get(module.primaryKey()));

        service.remove(module);
        assertNull(modules.get(module.primaryKey()));

        verify(actionsFactory, times(3)).startMainAction(eq(service));
    }

    @Test(expected = AssertionError.class)
    public void testRemoveFail() {
        service.add(module);

        Map<String, Module> modules = (Map<String, Module>) ReflectionTestUtils.getField(service, "modules");
        assertEquals(module, modules.get(module.primaryKey()));

        service.remove(null);
        fail("Here I'm waiting for an exception.");
    }
    @Test
    public void testRegistered() {
        service.add(module);

        List<Module> modules = new ArrayList<>(service.registered());
        assertTrue(modules.contains(module));
    }

    @Test
    public void testGetRegistered() {
        assertNull(service.getRegistered(module));

        service.add(module);
        assertEquals(module, service.getRegistered(module));
    }

    @Test(expected = AssertionError.class)
    public void testGetRegisteredFail() {
        service.getRegistered((Module)null);
        fail("Here I'm waiting for an exception.");
    }

    @Test
    public void testGetRegisteredPK() {
        assertNull(service.getRegistered(module.primaryKey()));
        service.add(module);

        assertEquals(module, service.getRegistered(module.primaryKey()));
        assertNull(service.getRegistered((String) null));
        assertNull(service.getRegistered(""));
    }

    @Test
    public void testInitAsService() {
        service.moduleStop();

        service.initAsService();

        assertEquals(service, service.getRegistered(service));
        ScheduledFuture runnerFuture = (ScheduledFuture) ReflectionTestUtils.getField(service, "runnerFuture");
        assertNotNull(runnerFuture);
        assertFalse(runnerFuture.isDone());
    }

    @Test
    public void testShutdownAsService() {
        service.shutdownAsService();

        assertNull(service.getRegistered(service));
        assertNull(ReflectionTestUtils.getField(service, "runnerFuture"));
    }

    @Test
    public void testConfigurationItemChanged() {
        Long delay = (Long) ReflectionTestUtils.getField(service, "delay");
        assertEquals(DELAY_DEFAULT, delay.longValue());

        service.configurationItemChanged(service.delayName(), new VariableItemDto(DELAY_NAME, 5000));
        delay = (Long) ReflectionTestUtils.getField(service, "delay");
        assertEquals(5000L, delay.longValue());

        String[] ignoredModules = (String[]) ReflectionTestUtils.getField(service, "ignoredModules");
        assertEquals(1, ignoredModules.length);
        assertEquals("", ignoredModules[0]);

        service.configurationItemChanged(service.ignoreModulesName(), new VariableItemDto(IGNORE_MODULE_NAME, "1,2,3"));
        ignoredModules = (String[]) ReflectionTestUtils.getField(service, "ignoredModules");
        assertEquals(3, ignoredModules.length);
        assertEquals("1", ignoredModules[0]);
        assertEquals("2", ignoredModules[1]);
        assertEquals("3", ignoredModules[2]);
    }

    @Test
    public void testInspectModule() {
        when(module.getMetricsContainer()).thenReturn(metricsContainer);

        service.inspectModule("test", mainAction, module);

        verify(module, times(1)).primaryKey();
        verify(module, times(1)).getMetricsContainer();
        verify(heartBeatMetricContainer, times(1)).heartBeat(eq(mainAction), eq(module));
        verify(module, times(1)).metrics();
        verify(durationMetricsContainer, times(1)).simple(eq("test"), eq(mainAction), any(Instant.class), eq("test-pk"), anyLong());
    }

    @Test
    public void testScanModulesHealth() {
        ModuleAction serviceAction = service.getMainAction();

        service.scanModulesHealth();

        verify(actionsFactory, times(2)).startMainAction(eq(service));
        assertEquals(service, service.getRegistered(service));
        ScheduledFuture runnerFuture = (ScheduledFuture) ReflectionTestUtils.getField(service, "runnerFuture");
        assertNotNull(runnerFuture);
        assertFalse(runnerFuture.isDone());
    }

    @Test
    public void store() {
        ModuleMetricAdapter metric = new ModuleMetricAdapter(mainAction, timeService.now()) {
            @Override
            public String name() {
                return "test-metric";
            }
        };
        service.store(metric);
        String modulePK = mainAction.getModule().primaryKey();
        String host = mainAction.getHostName();
        Instant measured = metric.measured();
        String data = metric.valuesAsString();

        verify(metricStorage, times(1))
                .storeMetric(eq("test-metric"), eq(modulePK), eq(measured), eq(host), eq(data));
    }
}