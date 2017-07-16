package com.mobenga.health.model;

import com.mobenga.health.model.business.out.ModuleOutputMessage;
import com.mobenga.health.model.business.out.log.ModuleLoggerMessage;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.StringTokenizer;

/**
 * The entity of module's log-message
 */
public class LogMessageEntity extends ModuleLoggerMessage implements StringEntity, Cloneable {
    private static final long serialVersionUID = 6631223050471982892L;
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
                + (getWhenOccurred() == null ? null : DATE_TIME_FORMATER.format(getWhenOccurred())) + "#"
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
            entity.setWhenOccured(DATE_TIME_FORMATER.parse(st.nextToken()));
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

    @Override
    public ModuleOutputMessage setModulePK(String modulePK) {
        super.modulePK = modulePK;
        return this;
    }

    /**
     * To create the copy of bean
     *
     * @return copy instance
     */
    @Override
    public ModuleOutputMessage copy() {
        try {
            return (LogMessageEntity)clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
