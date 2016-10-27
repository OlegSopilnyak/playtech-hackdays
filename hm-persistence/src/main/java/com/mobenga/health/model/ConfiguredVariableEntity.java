package com.mobenga.health.model;

import com.mobenga.health.model.persistence.ValidatingEntity;
import com.mobenga.health.monitor.strategy.VariableTypeStrategies;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * The entity for persistence
 */
public class ConfiguredVariableEntity extends ConfiguredVariableItem implements ValidatingEntity, Cloneable, StringEntity  {
    private TreeMap<String, Object> entity = new TreeMap<>();

    /**
     * The name of storage for this sort of beans
     *
     * @return the name
     */
    @Override
    public String storageName() {
        return STORAGE_NAME;
    }
    /**
     * To make copy of the entity
     * @return clone of entity
     */
    public ConfiguredVariableEntity copy(){
        try {
            final ConfiguredVariableEntity copy = (ConfiguredVariableEntity) super.clone();
            copy.entity = (TreeMap<String, Object>) entity.clone();
            return copy;
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
    /**
     * To copy item to entity
     *
     * @param value value to copy
     */
    public void apply(ConfiguredVariableItem value) {
        if (!value.isValid()){
            throw new IllegalArgumentException("Wrong item:"+value+" to apply!");
        }
        setName(value.getName());
        setDescription(value.getDescription());
        setType(value.getType());
        setValue(value.getValue());
    }
    /**
     * To validate internal state of entity if state invalid throws EntityInvalidState
     */
    @Override
    public void validate() {
        if(StringUtils.isEmpty(getModuleKey())){
            throw new EntityInvalidState("moduleKey", "The moduleKey is empty");
        }
        if(StringUtils.isEmpty(getPackageKey())){
            throw new EntityInvalidState("packageKey", "The packageKey is empty");
        }
        if(StringUtils.isEmpty(getName())){
            throw new EntityInvalidState("name", "The name is empty");
        }
        if(StringUtils.isEmpty(getDescription())){
            throw new EntityInvalidState("description", "The description is empty");
        }
        if (getType() == null){
            throw new EntityInvalidState("type", "The type is null");
        }
    }
    @Override
    public String toString() {
        return getId()+"#"
                +getModuleKey()+"#"
                +getPackageKey()+"#"
                +getName()+"#"
                +getDescription()+"#"+
                getType()+"#"
                +getVersion()+"#"
                +getValue();
    }
    @Override
    public StringEntity fromString(String value) {
        StringTokenizer st = new StringTokenizer(value, "#");
        ConfiguredVariableEntity recovered = new ConfiguredVariableEntity();
        recovered.setId(st.nextToken());
        recovered.setModuleKey(st.nextToken());
        recovered.setPackageKey(st.nextToken());
        recovered.setName(st.nextToken());
        recovered.setDescription(st.nextToken());
        recovered.setType(Type.valueOf(st.nextToken()));
        recovered.setVersion(Integer.parseInt(st.nextToken()));
        recovered.setValue(st.nextToken());
        recovered.strategy = VariableTypeStrategies.get(recovered.getType());
        return recovered;
    }


    @Override
    public String getName() {
        return (String) entity.get("name");
    }

    @Override
    public void setName(String name) {
        entity.put("name", name);
    }

    @Override
    public String getDescription() {
        return (String) entity.get("description");
    }

    @Override
    public void setDescription(String description) {
        entity.put("description", description);
    }

    @Override
    public String getValue() {
        return (String) entity.get("value");
    }

    @Override
    public void setValue(String value) {
        entity.put("value", value);
    }

    @Override
    public Type getType() {
        return (Type) entity.get("type");
    }

    @Override
    public void setType(Type type) {
        entity.put("type", type);
        strategy = VariableTypeStrategies.get(type);
    }
    public String getId() {
        return (String) entity.get("id");
    }

    public void setId(String id) {
        entity.put("id", id);
    }

    public String getModuleKey() {
        return (String) entity.get("moduleKey");
    }

    public void setModuleKey(String moduleKey) {
        entity.put("moduleKey", moduleKey);
    }

    public String getPackageKey() {
        return (String) entity.get("packageKey");
    }

    public void setPackageKey(String packageKey) {
        entity.put("packageKey", packageKey);
    }
    public int getVersion() {
        final Integer version = (Integer) entity.get("version");
        return version == null ? -1 : version.intValue();
    }

    public void setVersion(int version) {
        entity.put("version", version);
    }

    public String getMapKey(){
        return new StringBuffer(getPackageKey()).append(".").append(getName()).toString();
    }

}
