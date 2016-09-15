package com.mobenga.health.monitor.strategy.impl;

import com.mobenga.health.monitor.strategy.VariableTypeStrategy;

/**
 * Strategy for Double
 * @see  VariableTypeStrategy
 */
public class DoubleVariableTypeStrategy implements VariableTypeStrategy {
    @Override
    public String toString(Object value) {
        if (value instanceof  Number){
            return value.toString();
        }
        throw new IllegalArgumentException("The value is not a Double '"+value+"'");
    }

    @Override
    public <T> T toValue(Class<T> type, String stringValue) {
        if (Number.class.isAssignableFrom(type)) {
            return (T) toValue(stringValue);
        }
        throw new IllegalArgumentException("The value is not converted to '"+type.getCanonicalName()+"'");
    }

    @Override
    public Object toValue(String stringValue) {
        try {
            return Double.valueOf(stringValue);
        }catch(NumberFormatException e){
            throw new IllegalArgumentException("Wrong digits format '"+stringValue+"'");
        }
    }

    @Override
    public String defaultValue() {
        return Double.valueOf(0).toString();
    }
    
    @Override
    public boolean isValue(String value) {
        try{
            toValue(value);
        }catch(IllegalArgumentException e){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DoubleVariableTypeStrategy{}";
    }

}
