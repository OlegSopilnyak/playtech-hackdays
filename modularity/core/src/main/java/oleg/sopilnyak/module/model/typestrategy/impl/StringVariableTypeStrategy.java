/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.typestrategy.impl;

import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategy;

/**
 * Strategy for String
 * @see  VariableTypeStrategy
 */
public class StringVariableTypeStrategy implements VariableTypeStrategy {
    @Override
    public String asString(Object value) {
        if (value instanceof  String){
            return (String)value;
        }
        throw new IllegalArgumentException("The value is not a String '"+value+"'");
    }

    @Override
    public <T> T convert(Class<T> type, String stringValue) {
        if (type == String.class) {
            return (T) convert(stringValue);
        }
        throw new IllegalArgumentException("The value is not converted to '"+type.getCanonicalName()+"'");
    }

    @Override
    public Object convert(String stringValue) {
        return stringValue;
    }

    @Override
    public String defaultValue() {
        return "";
    }

    @Override
    public boolean isValid(String value) {
        return true;
    }

    @Override
    public String toString() {
        return "StringVariableTypeStrategy{}";
    }
    
}
