package com.mobenga.health.monitor.strategy;

/**
 * The strategy for configured variables
 */
public interface VariableTypeStrategy {
    /**
     * Properly convert the value to string
     *
     * @param value the value to convert
     * @return string or throw IllegalArgumentException
     */
    String toString(Object value);

    /**
     * Properly convert string value to required
     *
     * @param type reqired type
     * @param stringValue the value as string
     * @return the value casted to required type
     */
    <T> T toValue(Class<T> type, String stringValue);

    /**
     * Convert string value to appropriate type
     *
     * @param stringValue
     * @return the value in appropriate type
     */
    Object toValue(String stringValue);

    /**
     * The default value of the Type
     *
     * @return default value
     */
    String defaultValue();

    /**
     * To check if string presentation valid for strategy's type
     * 
     * @param value string value to check
     * @return true if value suit strategy's type
     */
    boolean isValue(String value);
}
