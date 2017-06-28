package com.mobenga.hm.openbet.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Transport object for module actions joint
 */
public class ModuleAction  implements Serializable{

    private static final long serialVersionUID = 788030296303559806L;
    private String description;
    private String state;
    private long duration;
    private String startTime;
    private String finishTime;
    private List<ModuleOutputMessage> output;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public List<ModuleOutputMessage> getOutput() {
        return output;
    }

    public void setOutput(List<ModuleOutputMessage> output) {
        this.output = output;
    }
}
