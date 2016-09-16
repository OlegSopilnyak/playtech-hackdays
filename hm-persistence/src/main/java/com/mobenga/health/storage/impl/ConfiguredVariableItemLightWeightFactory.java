package com.mobenga.health.storage.impl;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;

import java.util.HashMap;
import java.util.Map;

/**
 * The light-weight components factory for variable items reflected to
 * database<br/>
 * #Singleton
 */
public final class ConfiguredVariableItemLightWeightFactory {

    private static final Map<String, ConfiguredVariableItem> cache = new HashMap<>();

    private ConfiguredVariableItemLightWeightFactory() {
    }

    public static ConfiguredVariableItem itemFor(String modulePK, String packageKey, ConfiguredVariableItem original) {
        final String cacheKey = 
                new StringBuilder(modulePK)
                        .append("|").append(packageKey)
                        .append(".").append(original.getName())
                        .toString();
        synchronized(cache){
            final ConfiguredVariableItem item = 
                    cache.computeIfAbsent(cacheKey, k -> new LocalConfiguredVariableItem(original));
            item.setType(original.getType());
            item.setValue(original.getValue());
            return item;
        }
    }
}
