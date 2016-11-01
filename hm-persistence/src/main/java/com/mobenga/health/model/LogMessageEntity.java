package com.mobenga.health.model;

import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

/**
 * The entity of module's log-message
 */
public class LogMessageEntity extends LogMessage implements StringEntity, Cloneable {
    private static final SimpleDateFormat dateConverter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
    /**
     * The name of storage for this sort of beans
     *
     * @return the name
     */
    @Override
    public String storageName() {
        return STORAGE_NAME;
    }

    @Override
    public String toString() {
        return getId() +"#"
                + getModulePK() + "#"
                + getActionId() + "#"
                + (getWhenOccurred() == null ? null : dateConverter.format(getWhenOccurred())) + "#"
                + getPayload()
                ;
    }

    @Override
    public StringEntity fromString(String value) {
        LogMessageEntity entity = new LogMessageEntity();
        StringTokenizer st = new StringTokenizer(value, "#");
        entity.setId(st.nextToken());
        entity.modulePK = st.nextToken();
        entity.setActionId(st.nextToken());
        try {
            entity.setWhenOccured(dateConverter.parse(st.nextToken()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        entity.setPayload(st.nextToken());
        return entity;
    }

    /**
     * To validate internal state of entity if state invalid throws EntityInvalidState
     */
    @Override
    public void validate() {
        if(StringUtils.isEmpty(getId())){
            throw new EntityInvalidState("id", "The id is empty");
        }
        if(StringUtils.isEmpty(getPayload())){
            throw new EntityInvalidState("payload", "The payload is empty");
        }
        if(getWhenOccurred() == null){
            throw new EntityInvalidState("whenOccurred", "The whenOccurred is empty");
        }
    }

    public ModuleOutput setModulePK(String modulePK) {
        super.modulePK = modulePK;
        return this;
    }

    /**
     * To create the copy of bean
     *
     * @return copy instance
     */
    @Override
    public ModuleOutput copy() {
        try {
            return (LogMessageEntity)clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
