/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.typestrategy.impl;

import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategy;

/**
 * Strategy for Integer
 *
 * @see VariableTypeStrategy
 */
public class IntegerVariableTypeStrategy implements VariableTypeStrategy {
	@Override
	public String asString(Object value) {
		if (value instanceof Number) {
			return value.toString();
		}
		throw new IllegalArgumentException("The value is not an Integer '" + value + "'");
	}

	@Override
	public <T> T convert(Class<T> type, String stringValue) {
		if (Number.class.isAssignableFrom(type)) {
			return (T) convert(stringValue);
		}
		throw new IllegalArgumentException("The value is not converted to '" + type.getCanonicalName() + "'");
	}

	@Override
	public Number convert(String stringValue) {
		try {
			return Integer.valueOf(stringValue);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Wrong digits format '" + stringValue + "'");
		}
	}

	@Override
	public String defaultValue() {
		return Integer.toString(0);
	}

	@Override
	public boolean isValid(String value) {
		try {
			convert(value);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "IntegerVariableTypeStrategy{}";
	}

}
