package com.mobenga.health.model.transport;

import com.mobenga.health.model.business.HeartBeat;
import com.mobenga.health.model.business.ModuleHealth;

import java.util.Date;

/**
 * Transport object for hear-beat context
 */
public class HeartBeatDto implements HeartBeat{

    private String moduleKeyPK;
    private Date time;
    private String hostName;
    private String hostAddress;
    private ModuleHealth.Condition condition;
    /**
     * The reference to healthPK
     *
     * @return value of PK
     */
    @Override
    public String getModuleKeyPK() {
        return moduleKeyPK;
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
     * Condition of module's health
     *
     * @return value
     */
    @Override
    public ModuleHealth.Condition getCondition() {
        return condition;
    }

    public void setModuleKeyPK(String moduleKeyPK) {
        this.moduleKeyPK = moduleKeyPK;
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

    public void setCondition(ModuleHealth.Condition condition) {
        this.condition = condition;
    }
}
