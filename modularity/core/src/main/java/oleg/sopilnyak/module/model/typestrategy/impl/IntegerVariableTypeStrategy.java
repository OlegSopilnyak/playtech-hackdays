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
public class IntegerVariableTypeStrategy extends NumberVariableTypeStrategy {
	@Override
	public String asString(Object value) {
		if (value instanceof Number) {
			return Integer.toString(((Number)value).intValue());
		}
		throw new IllegalArgumentException("The value is not an Integer '" + value + "'");
	}

	@Override
	public Number convert(String stringValue) {
		try {
			return Double.valueOf(stringValue).intValue();
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
