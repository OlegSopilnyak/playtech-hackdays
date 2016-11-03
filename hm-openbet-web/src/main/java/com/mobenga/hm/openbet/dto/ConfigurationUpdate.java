package com.mobenga.hm.openbet.dto;

import java.util.List;

/**
 * DTO for batch configuration update
 */
public class ConfigurationUpdate {
    // the source of update request
    private String host;
    // module-id as string
    private String modulePK;
    // items to update
    private List<ModuleConfigurationItem> updated;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getModulePK() {
        return modulePK;
    }

    public void setModulePK(String modulePK) {
        this.modulePK = modulePK;
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
                ", modulePK='" + modulePK + '\'' +
                ", updated=" + updated +
                '}';
    }
}
