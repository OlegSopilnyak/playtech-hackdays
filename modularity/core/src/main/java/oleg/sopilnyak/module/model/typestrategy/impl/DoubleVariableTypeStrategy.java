/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.typestrategy.impl;

import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategy;

/**
 * Strategy for Double
 * @see  VariableTypeStrategy
 */
public class DoubleVariableTypeStrategy extends NumberVariableTypeStrategy {
    @Override
    public String asString(Object value) {
        if (value instanceof  Number){
            return value.toString();
        }
        throw new IllegalArgumentException("The value is not a Double '"+value+"'");
    }

    @Override
    public Object convert(String stringValue) {
        try {
            return Double.valueOf(stringValue).doubleValue();
        }catch(NumberFormatException e){
            throw new IllegalArgumentException("Wrong digits format '"+stringValue+"'");
        }
    }

    @Override
    public String defaultValue() {
        return Double.toString(0.0);
    }
    
    @Override
    public boolean isValid(String value) {
        try{
            convert(value);
            return true;
        }catch(IllegalArgumentException e){
            return false;
        }
    }

    @Override
    public String toString() {
        return "DoubleVariableTypeStrategy{}";
    }

}
