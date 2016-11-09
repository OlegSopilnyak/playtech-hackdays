package com.mobenga.health.model.transport;

import com.mobenga.health.model.HealthItemPK;

import java.util.Objects;

/**
 * Wrapper for module to transport
 */
public class ModuleWrapper implements HealthItemPK {
    final String systemId;
    final String applicationId;
    final String versionId;
    final String desription;

    public ModuleWrapper(HealthItemPK module) {
        this.systemId = module.getSystemId();
        this.applicationId = module.getApplicationId();
        this.versionId = module.getVersionId();
        this.desription = module.getDescription();
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
        return desription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof HealthItemPK)) return false;
        final HealthItemPK that = (HealthItemPK) o;
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
                ", desription='" + desription + '\'' +
                '}';
    }
}
