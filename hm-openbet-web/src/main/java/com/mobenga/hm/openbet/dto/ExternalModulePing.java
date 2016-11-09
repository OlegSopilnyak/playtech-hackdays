package com.mobenga.hm.openbet.dto;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.transport.ModuleWrapper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * DTO. Ping from external module
 */
@XmlRootElement
public class ExternalModulePing {
    // the wrapper of real module
    private ModuleWrapper module;
    // the host where module works
    private String host;
    // The module's state (active|passive)
    private String state;
    // Current configuration of module
    private List<ModuleConfigurationItem> configuration;
    // the output of module from last ping (without actions)
    private List<ModuleOutputMessage> output;
    // the ouput of module from last ping (with related actions
    private List<ModuleAction> actions;

    @XmlElement
    public ModuleWrapper getModule() {
        return module;
    }

    public void setModule(HealthItemPK module) {
        this.module = new ModuleWrapper(module);
    }

    @XmlElement
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @XmlElement
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @XmlElement
    public List<ModuleConfigurationItem> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(List<ModuleConfigurationItem> configuration) {
        this.configuration = configuration;
    }

    @XmlElement
    public List<ModuleOutputMessage> getOutput() {
        return output;
    }

    public void setOutput(List<ModuleOutputMessage> output) {
        this.output = output;
    }

    @XmlElement
    public List<ModuleAction> getActions() {
        return actions;
    }

    public void setActions(List<ModuleAction> actions) {
        this.actions = actions;
    }

    @Override
    public String toString() {
        return "ExternalModulePing{" +
                "module='" + module + '\'' +
                ", host='" + host + '\'' +
                ", state='" + state + '\'' +
                ", configuration=" + configuration +
                ", output=" + output +
                ", actions=" + actions +
                '}';
    }
}
