package com.mobenga.hm.openbet.dto;

/**
 * The output message from module
 */
public class ModuleOutputMessage {
    // the type of output message
    String messageType;
    // date-time when action related to message was occurred
    String whenOccurred;
    // the payload of message
    String payload;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getWhenOccurred() {
        return whenOccurred;
    }

    public void setWhenOccurred(String whenOccurred) {
        this.whenOccurred = whenOccurred;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "ModuleOutputMessage{" +
                "messageType='" + messageType + '\'' +
                ", whenOccurred='" + whenOccurred + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
