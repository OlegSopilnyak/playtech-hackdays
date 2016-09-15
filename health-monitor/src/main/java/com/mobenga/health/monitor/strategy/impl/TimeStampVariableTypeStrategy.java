package com.mobenga.health.monitor.strategy.impl;

import com.mobenga.health.monitor.strategy.VariableTypeStrategy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Strategy for TimeStamp
 *
 * @see VariableTypeStrategy
 */
public class TimeStampVariableTypeStrategy implements VariableTypeStrategy {
    private static final SimpleDateFormat dateConverter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public String toString(Object value) {
        if (value instanceof Date) {
            return dateConverter.format((Date) value);
        }
        throw new IllegalArgumentException("The value is not a Double '" + value + "'");
    }

    @Override
    public <T> T toValue(Class<T> type, String stringValue) {
        if (type == Date.class) {
            return (T) toValue(stringValue);
        }
        throw new IllegalArgumentException("The value is not converted to '" + type.getCanonicalName() + "'");
    }

    @Override
    public Object toValue(String stringValue) {
        try {
            return dateConverter.parse(stringValue);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Wrong date-time format '" + stringValue + "'");
        }
    }

    @Override
    public String defaultValue() {
        return dateConverter.format(new Date(0));
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
        return "TimeStampVariableTypeStrategy{}";
    }
}
