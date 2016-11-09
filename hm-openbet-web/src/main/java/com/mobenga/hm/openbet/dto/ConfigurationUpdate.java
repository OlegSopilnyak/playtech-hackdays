package com.mobenga.hm.openbet.dto;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.transport.ModuleWrapper;

import java.util.List;

/**
 * DTO for batch configuration update
 */
public class ConfigurationUpdate {
    // the source of update request
    private String host;
    // module as wrapper
    private ModuleWrapper module;
    // items to update
    private List<ModuleConfigurationItem> updated;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public HealthItemPK getModule() {
        return module;
    }

    public void setModule(HealthItemPK module) {
        this.module = new ModuleWrapper(module);
    }

    public List<ModuleConfigurationItem> getUpdated() {
        return updated;
    }

    public void setUpdated(List<ModuleConfigurationItem> updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "ConfigurationUpdate{" +
                "host='" + host + '\'' +
                ", module='" + module + '\'' +
                ", updated=" + updated +
                '}';
    }
}
