package com.mobenga.health.configuration;

import com.mobenga.health.model.LogMessage;
import com.mobenga.health.model.LogMessageEntity;
import com.mobenga.health.model.ModuleOutput;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.factory.UniqueIdGenerator;
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
        final Map<String, ModuleOutput> moduleOutputMap = new HashMap<>();
        moduleOutputMap.put(LogMessage.OUTPUT_TYPE, new LogMessageEntity());
        storage.setModuleOutputMap(moduleOutputMap);
        return storage;
    }

}
