package oleg.sopilnyak.service.healthcheck.impl;

import oleg.sopilnyak.configuration.ModuleSystemConfiguration;
import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.DurationMetricsContainer;
import oleg.sopilnyak.module.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.storage.ModuleStorage;
import oleg.sopilnyak.service.ServiceModule;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.action.bean.ActionMapper;
import oleg.sopilnyak.service.action.bean.ModuleActionAdapter;
import oleg.sopilnyak.service.action.impl.ModuleActionFactoryImpl;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.metric.ModuleMetricAdapter;
import oleg.sopilnyak.service.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.service.model.dto.VariableItemDto;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static oleg.sopilnyak.service.healthcheck.ModulesHeartBeatService.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModulesHeartBeatServiceImplTest {

    @Mock
    private Module module;
    @Mock
    private ModuleStorage moduleStorage;
    @Mock
    private MetricsContainer metricsContainer;
    @Mock
    private ActionMetricsContainer actionMetricsContainer;
    @Mock
    private DurationMetricsContainer durationMetricsContainer;
    @Mock
    private HeartBeatMetricContainer heartBeatMetricContainer;
    @Spy
    private ModuleActionAdapter mainAction;
    @Mock
    private ModuleConfigurationStorage configurationStorage;
    @Spy
    private TimeService timeService = new ModuleUtilityConfiguration().getTimeService();
    @Spy
    private ModuleActionFactory actionsFactory = new ModuleSystemConfiguration().getModuleActionFactory();
    @Mock
    private ModuleActionStorage actionStorage;
    @Spy
    protected ScheduledExecutorService activityRunner = new ModuleUtilityConfiguration().getScheduledExecutorService();
    @Mock
    private ModuleMetricStorage metricStorage;
    @Mock
    private ModulesRegistryService registry;
    @Spy
    @InjectMocks
    private ModulesHeartBeatServiceImpl service = new ModulesHeartBeatServiceImpl();

    @Before
    public void setUp() throws Exception {
        when(module.getSystemId()).thenReturn("sys-test");
        when(module.getModuleId()).thenReturn("mod-test");
        when(module.getVersionId()).thenReturn("ver-test");
        when(module.getDescription()).thenReturn("desc-test");
        when(module.primaryKey()).thenReturn("test-pk");
        mainAction = ActionMapper.INSTANCE.simple(module, "test");
        when(metricsContainer.action()).thenReturn(actionMetricsContainer);
        when(metricsContainer.duration()).thenReturn(durationMetricsContainer);
        when(metricsContainer.health()).thenReturn(heartBeatMetricContainer);

        prepareActionsFactory();

        ReflectionTestUtils.setField(service, "moduleMainAction", null);

        ((ModuleActionFactoryImpl) actionsFactory).setUp();
        service.moduleStart();
    }

    @After
    public void tearDown() throws Exception {
        service.moduleStop();
        reset(module, moduleStorage, actionStorage, actionsFactory, metricsContainer, actionMetricsContainer);
    }

    @Test
    public void testInitAsService() {
        service.moduleStop();

        service.initAsService();

        ScheduledFuture runnerFuture = (ScheduledFuture) ReflectionTestUtils.getField(service, "runnerFuture");
        assertNotNull(runnerFuture);
        assertFalse(runnerFuture.isDone());
    }

    @Test
    public void testShutdownAsService() {
        service.shutdownAsService();
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
//        assertEquals(service, service.getRegistered(service));
        ScheduledFuture runnerFuture = (ScheduledFuture) ReflectionTestUtils.getField(service, "runnerFuture");
        assertNotNull(runnerFuture);
        assertFalse(runnerFuture.isDone());
    }

    @Test
    public void testStore() {
        final Instant mark = timeService.now();
        ModuleMetricAdapter metric = new ModuleMetricAdapter() {
            @Override
            public String getName() {
                return "test-metric";
            }

            @Override
            public ModuleAction getAction() {
                return mainAction;
            }

            @Override
            public Instant getMeasured() {
                return mark;
            }
        };
        service.storeMetric(metric);
        String modulePK = mainAction.getModule().primaryKey();
        String host = mainAction.getHostName();
        Instant measured = metric.getMeasured();
        String data = metric.valuesAsString();

        verify(metricStorage, times(1))
                .storeMetric(eq("test-metric"), eq(modulePK),  eq(measured), eq(host), eq(mainAction.getId()), eq(data));
    }
    // private methods
    private void prepareActionsFactory() {
        ModuleActionStorage actionStorage = mock(ModuleActionStorage.class);
        ReflectionTestUtils.setField(actionsFactory, "timeService", timeService);
        ReflectionTestUtils.setField(actionsFactory, "scanRunner", activityRunner);
        ReflectionTestUtils.setField(actionsFactory, "delay", 200L);
        ReflectionTestUtils.setField(actionsFactory, "actionsStorage", actionStorage);
        when(actionStorage.createActionFor(any(Module.class)))
                .thenAnswer((Answer<ModuleAction>) invocation -> ActionMapper.INSTANCE.simple((ModuleBasics) invocation.getArguments()[0], "main-test"));
        when(actionStorage.createActionFor(any(ServiceModule.class), any(ModuleAction.class), anyString())).thenAnswer((Answer<ModuleAction>) invocation -> {
            ModuleActionAdapter result1 = ActionMapper.INSTANCE.simple((ModuleBasics) invocation.getArguments()[0], "regular-test");
            result1.setParent((ModuleAction) invocation.getArguments()[1]);
            result1.setName((String) invocation.getArguments()[2]);
            return result1;
        });
    }

}