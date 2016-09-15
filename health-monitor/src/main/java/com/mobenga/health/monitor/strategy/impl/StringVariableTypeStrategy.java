package com.mobenga.health.monitor.strategy.impl;

import com.mobenga.health.monitor.strategy.VariableTypeStrategy;

/**
 * Strategy for String
 * @see  VariableTypeStrategy
 */
public class StringVariableTypeStrategy implements VariableTypeStrategy {
    @Override
    public String toString(Object value) {
        if (value instanceof  String){
            return (String)value;
        }
        throw new IllegalArgumentException("The value is not a String '"+value+"'");
    }

    @Override
    public <T> T toValue(Class<T> type, String stringValue) {
        if (type == String.class) {
            return (T) toValue(stringValue);
        }
        throw new IllegalArgumentException("The value is not converted to '"+type.getCanonicalName()+"'");
    }

    @Override
    public Object toValue(String stringValue) {
        return stringValue;
    }

    @Override
    public String defaultValue() {
        return "";
    }

    @Override
    public boolean isValue(String value) {
        return true;
    }

    @Override
    public String toString() {
        return "StringVariableTypeStrategy{}";
    }
    
}
