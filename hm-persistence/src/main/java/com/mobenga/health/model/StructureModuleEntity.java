package com.mobenga.health.model;

import com.mobenga.health.model.persistence.ValidatingEntity;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.StringTokenizer;

/**
 * The entity to keep information about module. The owner of monitored-action and heard-beats<BR/>
 */
public class StructureModuleEntity implements HealthItemPK, ValidatingEntity, Cloneable, StringEntity  {
    // the ID of module-descriptor
    private String id;

    private String systemId;

    private String applicationId;

    private String versionId;

    private String description;


    public StructureModuleEntity() {
    }
    public StructureModuleEntity(String moduleId){
        super();
        StringTokenizer st = new StringTokenizer(moduleId, "|");
        systemId = st.nextToken();
        applicationId = st.nextToken();
        versionId = st.nextToken();
        description = moduleId;
    }

    @Override
    public String toString() {
        return id + "#"
                +systemId + "#"
                +applicationId + "#"
                +versionId + "#"
                +description
                ;
    }

    @Override
    public StringEntity fromString(String value) {
        StructureModuleEntity enity = new StructureModuleEntity();
        StringTokenizer st = new StringTokenizer(value, "#");
        enity.setId(st.nextToken());
        enity.setSystemId(st.nextToken());
        enity.setApplicationId(st.nextToken());
        enity.setVersionId(st.nextToken());
        enity.setDescription(st.nextToken());
        return enity;
    }

    public StructureModuleEntity(HealthItemPK module){
        this.systemId = module.getSystemId();
        this.applicationId = module.getApplicationId();
        this.versionId = module.getVersionId();
        this.description = module.getDescription();
    }

    /**
     * To validate internal state of entity if state invalid throws EntityInvalidState
     */
    @Override
    public void validate() {
        if(StringUtils.isEmpty(systemId)){
            throw new EntityInvalidState("systemId", "The systemId is empty");
        }
        if(StringUtils.isEmpty(applicationId)){
            throw new EntityInvalidState("applicationId", "The applicationId is empty");
        }
        if(StringUtils.isEmpty(versionId)){
            throw new EntityInvalidState("versionId", "The versionId is empty");
        }
        if(StringUtils.isEmpty(versionId)){
            throw new EntityInvalidState("description", "The description is empty");
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * to get the value of item's system
     *
     * @return the value
     */
    @Override
    public String getSystemId() {
        return systemId;
    }

    /**
     * to get the value of item's application
     *
     * @return the value
     */
    @Override
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * to get the value of item's application version
     *
     * @return the value
     */
    @Override
    public String getVersionId() {
        return versionId;
    }

    /**
     * to get description of module
     *
     * @return the value
     */
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
        HealthItemPK that = (HealthItemPK) o;
        return Objects.equals(getSystemId(), that.getSystemId()) &&
                Objects.equals(getApplicationId(), that.getApplicationId()) &&
                Objects.equals(getVersionId(), that.getVersionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSystemId(), getApplicationId(), getVersionId());
    }

}
