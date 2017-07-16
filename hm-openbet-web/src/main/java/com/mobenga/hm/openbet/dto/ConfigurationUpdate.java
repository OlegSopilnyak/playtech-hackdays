package com.mobenga.hm.openbet.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.transport.ModuleKeyDto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for batch configuration update
 */
public class ConfigurationUpdate implements Serializable{
    // the source of update request
    private String host;
    // module as wrapper
    private ModuleKeyDto module;
    // items to update
    private List<ModuleConfigurationItem> updated;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @JsonDeserialize(as = ModuleKeyDto.class)
    public ModuleKey getModule() {
        return module;
    }

    public void setModule(ModuleKey module) {
        this.module = new ModuleKeyDto(module);
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
