package com.mobenga.hm.openbet.dto;

import java.util.List;

/**
 * Ping from external module
 */
public class ExternalModulePing {
    // the pipeline separated parameters of the module (sys,app,version)
    private String modulePK;
    // the host where module works
    private String host;
    // The module's state (active|passive)
    private String state;
    // Current configuration of module
    private List<ModuleConfigurationItem> configuration;
    // the output of module from last ping
    private List<ModuleOutputMessage> output;

    public String getModulePK() {
        return modulePK;
    }

    public void setModulePK(String modulePK) {
        this.modulePK = modulePK;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<ModuleConfigurationItem> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(List<ModuleConfigurationItem> configuration) {
        this.configuration = configuration;
    }

    public List<ModuleOutputMessage> getOutput() {
        return output;
    }

    public void setOutput(List<ModuleOutputMessage> output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "ExternalModulePing{" +
                "modulePK='" + modulePK + '\'' +
                ", host='" + host + '\'' +
                ", state='" + state + '\'' +
                ", configuration=" + configuration +
                ", output=" + output +
                '}';
    }
}
