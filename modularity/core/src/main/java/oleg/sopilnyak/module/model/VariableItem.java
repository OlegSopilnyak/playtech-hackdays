/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model;

import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategy;

/**
 * Type configured variable item
 */
public interface VariableItem {
	/**
	 * To get type of variable
	 *
	 * @return value
	 */
	Type type();

	/**
	 * To get name of variable
	 *
	 * @return value
	 */
	String name();

	/**
	 * Represent variable's value as string
	 *
	 * @return string
	 */
	String valueAsString();

	/**
	 * Setup new value of variable
	 *
	 * @param value new value
	 * @param <T>   type of value
	 */
	<T> void set(T value);

	/**
	 * To get value of required type
	 *
	 * @param requiredType class of required type
	 * @param <T>          the type of value (class)
	 * @return value of required type
	 */
	<T> T get(Class<T> requiredType);

	/**
	 * Types converter strategy for variable's value
	 *
	 * @return value
	 */
	VariableTypeStrategy strategy();

	// inner classes
	enum Type {
		STRING, INTEGER, DOUBLE, TIME_STAMP
//        ,XML, JSON
	}
}
