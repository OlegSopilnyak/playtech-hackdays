/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.impl;

import oleg.sopilnyak.configuration.ModuleSystemConfiguration;
import oleg.sopilnyak.configuration.ModuleUtilityConfiguration;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.ActionMetricsContainer;
import oleg.sopilnyak.module.metric.DurationMetricsContainer;
import oleg.sopilnyak.module.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.module.model.action.ResultModuleAction;
import oleg.sopilnyak.module.model.action.SuccessModuleAction;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.action.impl.ModuleActionFactoryImpl;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModuleConfigurationServiceImplTest {
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
    private ModulesRegistryService registry;
    @InjectMocks
    private ModuleConfigurationServiceImpl service = new ModuleConfigurationServiceImpl();
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
        reset(module, actionsFactory, metricsContainer, actionMetricsContainer, registry);
    }


    @Test
    public void initAsService() {
    }

    @Test
    public void shutdownAsService() {
    }

    @Test
    public void inspectModule() {
    }

    @Test
    public void scanModulesConfiguration() {
    }

    @Test
    public void runNotificationProcessing() {
    }

    @Test
    public void scheduleScan() {
    }

    @Test
    public void waitForFutureDone() {
    }

    @Test
    public void stopFuture() {
    }
}