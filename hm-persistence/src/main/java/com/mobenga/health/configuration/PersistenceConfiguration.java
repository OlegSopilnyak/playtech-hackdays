package com.mobenga.health.configuration;

import com.mobenga.health.model.LogMessageEntity;
import com.mobenga.health.model.business.out.ModuleOutputMessage;
import com.mobenga.health.model.business.out.log.ModuleLoggerMessage;
import com.mobenga.health.monitor.TimeService;
import com.mobenga.health.monitor.UniqueIdGenerator;
import com.mobenga.health.storage.impl.SimpleFileStorageImpl;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for monitor persistence
 */
@Configuration
@Import(FactoryConfiguration.class)
public class PersistenceConfiguration {

    @Autowired
    private TimeService timeService;
    @Autowired
    private UniqueIdGenerator idGenerator;

    @Bean(name = "coreStorage", autowire = Autowire.BY_TYPE)
    public SimpleFileStorageImpl monitorCoreStorage() {
        final SimpleFileStorageImpl storage = new SimpleFileStorageImpl();
        final Map<String, ModuleOutputMessage> moduleOutputMap = new HashMap<>();
        moduleOutputMap.put(ModuleLoggerMessage.LOG_OUTPUT_TYPE, new LogMessageEntity());
        storage.setModuleOutputMap(moduleOutputMap);
        return storage;
    }

}
