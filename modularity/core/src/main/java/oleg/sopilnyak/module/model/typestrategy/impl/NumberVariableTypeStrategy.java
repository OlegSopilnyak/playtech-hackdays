/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.typestrategy.impl;

import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategy;

/**
 * Strategy for Number
 *
 * @see VariableTypeStrategy
 */
abstract class NumberVariableTypeStrategy implements VariableTypeStrategy{
	/**
	 * Properly convert string value to required type
	 *
	 * @param type required type
	 * @param stringValue        the value as string
	 * @return the value casted to required type
	 */
	@Override
	public <T> T convert(Class<T> type, String stringValue) {
		if (Number.class.isAssignableFrom(type)) {
			Number value = (Number) convert(stringValue);
			if (Long.class.isAssignableFrom(type)){
				return (T)new Long(value.longValue());
			}
			if (Integer.class.isAssignableFrom(type)){
				return (T)new Integer(value.intValue());
			}
			if (Short.class.isAssignableFrom(type)){
				return (T)new Short(value.shortValue());
			}
			if (Byte.class.isAssignableFrom(type)){
				return (T)new Byte(value.byteValue());
			}
			if (Float.class.isAssignableFrom(type)){
				return (T)new Float(value.floatValue());
			}
			if (Double.class.isAssignableFrom(type)){
				return (T)new Double(value.doubleValue());
			}
		}
		throw new IllegalArgumentException("The value is not converted to '" + type.getCanonicalName() + "'");
	}
}
