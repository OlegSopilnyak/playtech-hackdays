/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategiesFactory;
import oleg.sopilnyak.module.model.typestrategy.VariableTypeStrategy;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Type: configured variable item
 *
 * @see oleg.sopilnyak.module.model.VariableItem
 */
public class VariableItemDto implements VariableItem, Serializable {
	@JsonProperty("type")
	private Type type;
	@JsonProperty("name")
	private String name;
	@JsonIgnore
	private transient VariableTypeStrategy strategy;
	@JsonProperty("value")
	private String valueAsString;

	public VariableItemDto() {
	}

	/**
	 * Constructor for String type
	 *
	 * @param name  name of variable
	 * @param value value of variable
	 */
	public VariableItemDto(String name, String value) {
		this(Type.STRING, name, VariableTypeStrategiesFactory.get(Type.STRING));
		this.valueAsString = strategy.asString(value);
	}

	/**
	 * Constructor for Integer type
	 *
	 * @param name  name of variable
	 * @param value value of variable
	 */
	public VariableItemDto(String name, Integer value) {
		this(Type.INTEGER, name, VariableTypeStrategiesFactory.get(Type.INTEGER));
		this.valueAsString = strategy.asString(value);
	}

	/**
	 * Constructor for Double type
	 *
	 * @param name  name of variable
	 * @param value value of variable
	 */
	public VariableItemDto(String name, Double value) {
		this(Type.DOUBLE, name, VariableTypeStrategiesFactory.get(Type.DOUBLE));
		this.valueAsString = strategy.asString(value);
	}

	/**
	 * Constructor for Date type
	 *
	 * @param name  name of variable
	 * @param value value of variable
	 */
	public VariableItemDto(String name, Date value) {
		this(Type.TIME_STAMP, name, VariableTypeStrategiesFactory.get(Type.TIME_STAMP));
		this.valueAsString = strategy.asString(value);
	}

	/**
	 * Constructor for exists VariableItem and string
	 *
	 * @param item            to be copied
	 * @param moduleItemValue new value of item
	 */
	public VariableItemDto(VariableItem item, String moduleItemValue) {
		this(item.type(), item.name(), item.strategy());
		strategy.convert(moduleItemValue);
		this.valueAsString = moduleItemValue;
	}

	/**
	 * To get type of variable
	 *
	 * @return value
	 */
	@Override
	public Type type() {
		return type;
	}

	/**
	 * To get name of variable
	 *
	 * @return value
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * Represent variable's value as string
	 *
	 * @return string
	 */
	@Override
	public String valueAsString() {
		return valueAsString;
	}

	/**
	 * Setup new value of variable
	 *
	 * @param value new value
	 */
	@Override
	public <T> void set(T value) {
		valueAsString = Objects.isNull(value) ? strategy.defaultValue() : strategy.asString(value);
	}

	/**
	 * To get value of required type
	 *
	 * @param requiredType target type
	 * @return value of required type
	 */
	@Override
	public <T> T get(Class<T> requiredType) {
		return strategy.convert(requiredType, valueAsString);
	}

	/**
	 * Types converter strategy for variable's value
	 *
	 * @return value
	 */
	@Override
	public VariableTypeStrategy strategy() {
		return strategy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VariableItemDto that = (VariableItemDto) o;
		return type == that.type &&
				name.equals(that.name) &&
				valueAsString.equals(that.valueAsString);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name, valueAsString);
	}

	@Override
	public String toString() {
		return "VariableItem{" +
				"'" + name + '\'' +
				": '" + valueAsString + '\'' +
				'}';
	}

	// private methods
	private VariableItemDto(Type type, String name, VariableTypeStrategy strategy) {
		this.type = type;
		this.name = name;
		this.strategy = strategy;
	}
}
