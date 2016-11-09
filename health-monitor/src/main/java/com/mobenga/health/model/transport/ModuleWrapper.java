package com.mobenga.health.model.transport;

import com.mobenga.health.model.HealthItemPK;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * Wrapper for module to transport
 */
@XmlRootElement
@XmlType(propOrder = {"systemId","applicationId", "versionId", "description"})
public class ModuleWrapper implements HealthItemPK {
    private String systemId;
    private String applicationId;
    private String versionId;
    private String description;

    public ModuleWrapper() {
    }

    public ModuleWrapper(HealthItemPK module) {
        this.systemId = module.getSystemId();
        this.applicationId = module.getApplicationId();
        this.versionId = module.getVersionId();
        this.description = module.getDescription();
    }

    @XmlElement
    @Override
    public String getSystemId() {
        return systemId;
    }
    @XmlElement
    @Override
    public String getApplicationId() {
        return applicationId;
    }
    @XmlElement
    @Override
    public String getVersionId() {
        return versionId;
    }
    @XmlElement
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
                ", description='" + description + '\'' +
                '}';
    }
}
