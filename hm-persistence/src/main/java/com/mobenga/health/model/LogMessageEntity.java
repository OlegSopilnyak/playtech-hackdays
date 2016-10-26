package com.mobenga.health.model;

/**
 * The entity of module's log-message
 */
public class LogMessageEntity extends LogMessage implements StringEntity {
    @Override
    public String toString() {
        return "";
    }

    @Override
    public StringEntity fromString(String value) {
        return null;
    }

    /**
     * To validate internal state of entity if state invalid throws EntityInvalidState
     */
    @Override
    public void validate() {

    }
}
