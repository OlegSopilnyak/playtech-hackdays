package com.mobenga.hm.openbet.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.transport.ModuleKeyDto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO. Ping from external module
 */
public class ExternalModulePing implements Serializable {
    // the wrapper of real module
    private ModuleKeyDto module;
    // the host where module works
    private String host;
    // The module's state (active|passive)
    private String state;
    // Current configuration of module
    private List<ModuleConfigurationItem> configuration;
    // the output of module from last ping (without actions)
    private List<ModuleOutputMessage> output;
    // the output of module from last ping (with related actions
    private List<ModuleAction> actions;

    @JsonDeserialize(as = ModuleKeyDto.class)
    public ModuleKeyDto getModule() {
        return module;
    }

    public void setModule(ModuleKey module) {
        this.module = new ModuleKeyDto(module);
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
