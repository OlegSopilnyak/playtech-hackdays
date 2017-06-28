package com.mobenga.health.model.transport;

import com.mobenga.health.model.ModuleOutput;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * The criteria to select ModuleOutput messages
 */
public class ModuleOutputCriteriaBase implements ModuleOutput.Criteria, Serializable {

    private static final long serialVersionUID = -6356691990805101060L;

    protected String outputId;
    protected String type;
    protected String modulePK;
    protected String[] actionIds;
    protected Date moreThan;
    protected Date lessThan;

    /**
     * Select one entity with appropriate id
     *
     * @return the value of id or null if ignored
     */
    @Override
    public String getOutputId() {
        return outputId;
    }

    /**
     * select all entities with appropriate type
     *
     * @return the value of type or null if ignored
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Select all entities of particular module
     *
     * @return the value or null if ignored
     */
    @Override
    public String getModulePK() {
        return modulePK;
    }

    /**
     * Select all entities which belong enumerated actions
     *
     * @return the array of action-id or empty if ignored
     */
    @Override
    public String[] getActionIds() {
        return actionIds;
    }

    /**
     * select all entities which happened after the date
     *
     * @return the value or null if ignored
     */
    @Override
    public Date getMoreThan() {
        return moreThan;
    }

    /**
     * select all entities which happened before the date
     *
     * @return the value or null if ignored
     */
    @Override
    public Date getLessThen() {
        return lessThan;
    }

    /**
     * To test is particular message suitable for the criteria
     *
     * @param message message to test
     * @return true if message is suitable
     */
    @Override
    public boolean isSuitable(ModuleOutput message) {
        if (message == null) return false;
        if (!StringUtils.isEmpty(getOutputId())) {
            if (!getOutputId().equals(message.getId())) return false;
        }
        if (!StringUtils.isEmpty(getType())) {
            if (!getType().equalsIgnoreCase(message.getMessageType())) return false;
        }
        if (!StringUtils.isEmpty(getModulePK())) {
            if (!getModulePK().equals(message.getModulePK())) return false;
        }
        if (getActionIds() != null && getActionIds().length > 0){
            if (!Arrays.asList(getActionIds()).contains(message.getActionId())) return false;
        }
        if (getMoreThan() != null){
            if (!message.getWhenOccurred().before(getMoreThan())) return false;
        }
        if (getLessThen() != null){
            if (!message.getWhenOccurred().after(getLessThen())) return false;
        }
        return true;
    }

    public void setOutputId(String outputId) {
        this.outputId = outputId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setModulePK(String modulePK) {
        this.modulePK = modulePK;
    }

    public void setActionIds(String[] actionIds) {
        this.actionIds = actionIds;
    }

    public void setMoreThan(Date moreThan) {
        this.moreThan = moreThan;
    }

    public void setLessThan(Date lessThan) {
        this.lessThan = lessThan;
    }

    @Override
    public String toString() {
        return "ModuleOutputCriteriaBase{" +
                "outputId='" + outputId + '\'' +
                ", type='" + type + '\'' +
                ", modulePK='" + modulePK + '\'' +
                ", actionIds=" + Arrays.toString(actionIds) +
                ", moreThan=" + moreThan +
                ", lessThan=" + lessThan +
                '}';
    }
}
