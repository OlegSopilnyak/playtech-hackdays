/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.typestrategy.impl;

import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategy;

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
    public String asString(Object value) {
        if (value instanceof Date) {
            return dateConverter.format((Date) value);
        }
        throw new IllegalArgumentException("The value is not a Double '" + value + "'");
    }

    @Override
    public <T> T convert(Class<T> type, String stringValue) {
        if (type == Date.class) {
            return (T) convert(stringValue);
        }
        throw new IllegalArgumentException("The value is not converted to '" + type.getCanonicalName() + "'");
    }

    @Override
    public Object convert(String stringValue) {
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
        return "TimeStampVariableTypeStrategy{}";
    }
}
