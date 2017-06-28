package com.mobenga.health.model.transport;

import com.mobenga.health.model.ModulePK;
import java.io.Serializable;

/**
 * Transport object for module's health
 */
public class ModuleHealthDto implements ModulePK, Serializable{

    private static final long serialVersionUID = -2049959624478697903L;
    // the ID of the system
    private String systemId;
    // the ID of the module
    private String applicationId;
    // the ID of module's version
    private String versionId;
    // The description of the module
    private String description;
    // Flag is module active now
    private boolean active;
    @Override
    public String getSystemId() {
        return systemId;
    }

    public ModuleHealthDto() {
    }

    public ModuleHealthDto(ModulePK module, boolean active) {
        systemId = module.getSystemId();
        applicationId = module.getApplicationId();
        versionId = module.getVersionId();
        description = module.getDescription();
        this.active = active;
    }

    @Override
    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public String getVersionId() {
        return versionId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean sameAs(ModulePK module){
        return systemId.equals(module.getSystemId()) &&
                applicationId.equals(module.getApplicationId()) &&
                versionId.equals(module.getVersionId())
                ;
    }

    @Override
    public String toString() {
        return "ModuleHealthItem{" +
                "systemId='" + systemId + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", versionId='" + versionId + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                '}';
    }
}
