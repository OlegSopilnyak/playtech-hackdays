/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model.typestrategy;

/**
 * Type - strategy for value converter
 */
public interface VariableTypeStrategy {
	/**
	 * Properly convert the value to string
	 *
	 * @param value the value to convert
	 * @return string or throw IllegalArgumentException
	 */
	String asString(Object value);

	/**
	 * Properly convert string value to required type
	 *
	 * @param <T> type of result convert variable to
	 * @param requiredType required type
	 * @param value the value as string
	 * @return the value casted to required type
	 */
	<T> T convert(Class<T> requiredType, String value);

	/**
	 * Convert string value to appropriate type
	 *
	 * @param value the value of item as string
	 * @return the value in appropriate type
	 */
	Object convert(String value);

	/**
	 * The default string value of the Type
	 *
	 * @return default value
	 */
	String defaultValue();

	/**
	 * To check if string presentation valid for strategy's type
	 *
	 * @param value string value to check
	 * @return true if value suit strategy's type
	 */
	boolean isValid(String value);
}
