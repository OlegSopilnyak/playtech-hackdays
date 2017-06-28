package com.mobenga.health.model.transport;

import java.io.Serializable;

import java.util.Objects;
import com.mobenga.health.model.ModulePK;

/**
 * Wrapper for module to transport
 */
public class ModuleKeyDto implements ModulePK, Serializable {

    private static final long serialVersionUID = 2636625287244847952L;
    private String systemId;
    private String applicationId;
    private String versionId;
    private String description;

    public ModuleKeyDto() {
    }

    public ModuleKeyDto(ModulePK module) {
        this.systemId = module.getSystemId();
        this.applicationId = module.getApplicationId();
        this.versionId = module.getVersionId();
        this.description = module.getDescription();
    }

    @Override
    public String getSystemId() {
        return systemId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ModulePK)) return false;
        final ModulePK that = (ModulePK) o;
        return Objects.equals(getSystemId(), that.getSystemId()) &&
                Objects.equals(getApplicationId(), that.getApplicationId()) &&
                Objects.equals(getVersionId(), that.getVersionId()) &&
                Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSystemId(), getApplicationId(), getVersionId(), getDescription());
    }

    @Override
    public String toString() {
        return "DTO for module {" +
                "systemId='" + systemId + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", versionId='" + versionId + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
