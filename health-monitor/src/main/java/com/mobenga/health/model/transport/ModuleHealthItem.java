package com.mobenga.health.model.transport;

import com.mobenga.health.model.HealthItemPK;

/**
 * Transport object for module's health
 */
public class ModuleHealthItem implements HealthItemPK{
    // the ID of the system
    private String systemId;
    // the ID of the application
    private String applicationId;
    // the ID of application's version
    private String versionId;
    // The description of the application
    private String description;
    // Flag is module active now
    private boolean active;
    @Override
    public String getSystemId() {
        return systemId;
    }

    public ModuleHealthItem() {
    }

    public ModuleHealthItem(HealthItemPK module, boolean active) {
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

    public boolean sameAs(HealthItemPK module){
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
