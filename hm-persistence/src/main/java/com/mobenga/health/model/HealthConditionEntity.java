package com.mobenga.health.model;

import com.mobenga.health.HealthUtils;
import com.mobenga.health.model.business.HeartBeat;
import com.mobenga.health.model.business.ModuleHealth;
import com.mobenga.health.model.business.ModuleKey;

import java.util.Date;
import java.util.StringTokenizer;

import static com.mobenga.health.HealthUtils.key;

/**
 * The entity of registry condition
 */
public class HealthConditionEntity implements HeartBeat, StringEntity {

    private final StructureModuleEntity module = new StructureModuleEntity();
    private Date time = new Date();
    private String hostName = "localhost";
    private String hostAddress = "127.0.0.1";
    private ModuleHealth.Condition condition = ModuleHealth.Condition.VERY_GOOD;

    /**
     * To store new value of id
     *
     * @param id new value
     */
    @Override
    public void setId(String id) {

    }

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
    public StringEntity fromString(String value) {
        final HealthConditionEntity entity = new HealthConditionEntity();
        StringTokenizer st = new StringTokenizer(value, "#");
        entity.applyModulePK(st.nextToken());
        entity.time = HealthUtils.fromString(st.nextToken());
        entity.hostName = st.nextToken();
        entity.hostAddress = st.nextToken();
        return entity;
    }
    @Override
    public String toString() {
        return getModuleKeyPK()+"#" +
                HealthUtils.fromDate(getTime())+"#"+
                getHostName()+"#"+
                getHostAddress()+"#"+
                "isModuleActive()"
                ;
    }

    public ModuleKey getModule() {
        return module;
    }


    public void setHealthPK(String pk){
        applyModulePK(pk);
    }
    public void setHealthPK(ModuleKey pk){
        applyModulePK(key(pk));
    }

    /**
     * The reference to healthPK
     *
     * @return value of PK
     */
    @Override
    public String getModuleKeyPK() {
        return key(module);
    }

    /**
     * Exact date-time of heard-beat
     *
     * @return the time
     */
    @Override
    public Date getTime() {
        return time;
    }

    /**
     * The host-name where heard beats
     *
     * @return the host-name
     */
    @Override
    public String getHostName() {
        return hostName;
    }

    /**
     * The host-address where heard beats
     *
     * @return the host-address
     */
    @Override
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * Condition of module's registry
     *
     * @return value
     */
    @Override
    public ModuleHealth.Condition getCondition() {
        return condition;
    }


    public void setTime(Date time) {
        this.time = time;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    // private methods
    private void applyModulePK(String pk){
        StringTokenizer st = new StringTokenizer(pk,"|");
        module.setSystemId(st.nextToken());
        module.setApplicationId(st.nextToken());
        module.setVersionId(st.nextToken());
    }

    public boolean changed(ModuleHealth module) {
//        return isModuleActive() != module.isActive();
        return false;
    }

}
