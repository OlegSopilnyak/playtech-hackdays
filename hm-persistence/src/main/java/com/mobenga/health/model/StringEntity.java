package com.mobenga.health.model;

/**
 * Mark for persistence
 */
public interface StringEntity {
    String toString();
    StringEntity fromString(String value);
}
