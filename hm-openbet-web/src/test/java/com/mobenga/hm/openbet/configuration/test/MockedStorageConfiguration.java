package com.mobenga.hm.openbet.configuration.test;

import com.hazelcast.core.HazelcastInstance;
import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.LogMessage;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.storage.*;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.StringTokenizer;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The configuration of mocked storage
 */
public class MockedStorageConfiguration {

    private Date now = new Date();
    @Autowired
    private MonitoredAction action;
    @Autowired
    private ConfiguredVariableItem item;
    @Autowired
    private LogMessage message;

    @Bean
    public OpenbetOperationsManipulationService createOperationsStorage(){
        OpenbetOperationsManipulationService service = mock(OpenbetOperationsManipulationService.class);
        return service;
    }

    @Bean
    public MonitoredAction mockedAction(){
        MonitoredAction action = mock(MonitoredAction.class);
        when(action.getStart()).thenReturn(now);
        when(action.getFinish()).thenReturn(now);
        return action;
    }

    @Bean
    public MonitoredActionStorage createActionStorage(){
        MonitoredActionStorage storage = mock(MonitoredActionStorage.class);
        when(storage.createMonitoredAction()).thenReturn(action);
        return storage;
    }

    @Bean
    public final ConfiguredVariableItem mockedVarItem(){
        ConfiguredVariableItem item = mock(ConfiguredVariableItem.class);
        return item;
    }

    @Bean
    public ConfigurationStorage createConfigStorage(){
        ConfigurationStorage storage = mock(ConfigurationStorage.class);
        when(storage.createVariableItem()).thenReturn(item);
        return storage;
    }

    @Bean
    public final LogMessage mockedLogMessage(){
        LogMessage message = mock(LogMessage.class);
        return message;
    }

    @Bean
    public ModuleOutputStorage createOutputStorage(){
        ModuleOutputStorage storage = mock(ModuleOutputStorage.class);
        when(storage.createModuleOutput(any(), eq(LogMessage.OUTPUT_TYPE))).thenReturn(message);
        return storage;
    }

    @Bean
    public HeartBeatStorage createHeartbeatStorage(){
        HeartBeatStorage storage = mock(HeartBeatStorage.class);
        return storage;
    }

    @Bean
    public HealthModuleStorage createModuleStorage(){
        HealthModuleStorage storage = mock(HealthModuleStorage.class);
        when(storage.getModulePK(any(HealthItemPK.class))).then(new Answer<HealthItemPK>() {
            @Override
            public HealthItemPK answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                return new ModuleStub((HealthItemPK) args[0]);
            }
        });
        when(storage.getModulePK(anyString())).then(new Answer<HealthItemPK>() {
            @Override
            public HealthItemPK answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                return new ModuleStub((String) args[0]);
            }
        });
        return storage;
    }
    // private inner classes
    private static class ModuleStub implements HealthItemPK{
        private String sysId        ;
        private String appId;
        private String verId;

        public ModuleStub(HealthItemPK module) {
            sysId = module.getSystemId();
            appId = module.getApplicationId();
            verId = module.getVersionId();
        }
        public ModuleStub(String moduleId) {
            StringTokenizer st = new StringTokenizer(moduleId,"|");
            sysId = st.nextToken();
            appId = st.nextToken();
            verId = st.nextToken();
        }

        @Override
        public String getSystemId() {
            return sysId;
        }
        @Override
        public String getApplicationId() {
            return appId;
        }
        @Override
        public String getVersionId() {
            return verId;
        }
        @Override
        public String getDescription() {
            return "Stub for "+sysId+"|"+appId+"|"+verId;
        }
    }

    @Bean
    public HazelcastInstance cache(){
        return mock(HazelcastInstance.class);
    }
}
