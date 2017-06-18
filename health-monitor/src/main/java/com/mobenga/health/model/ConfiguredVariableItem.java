package com.mobenga.health.model;

import com.mobenga.health.monitor.strategy.VariableTypeStrategies;
import com.mobenga.health.monitor.strategy.VariableTypeStrategy;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * The item to store value of variable for configuration sub-system
 */
public abstract class ConfiguredVariableItem implements Serializable {
    // the name of storage item (table/index-type/etc)
    public static final String STORAGE_NAME = "configuration-variable";
    private static final long serialVersionUID = 8838075550610081499L;

    protected transient VariableTypeStrategy strategy = null;

    protected ConfiguredVariableItem() {
    }

    private ConfiguredVariableItem(String name, String description, Object value, Type type) {
        this(name, description, type);
        Objects.requireNonNull(value);
        setValue(strategy.toString(value));
    }

    public ConfiguredVariableItem(String name, String description, Type type) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(description);
        Objects.requireNonNull(type);
        setName(name);
        setDescription(description);
        setType(type);
        strategy = VariableTypeStrategies.get(type);
        setValue(strategy.defaultValue());
    }

    public ConfiguredVariableItem(String name, String description, String value) {
        this(name, description, value, Type.STRING);
    }

    public ConfiguredVariableItem(String name, String description, Integer value) {
        this(name, description, value, Type.INTEGER);
    }

    public ConfiguredVariableItem(String name, String description, Double value) {
        this(name, description, value, Type.DOUBLE);
    }

    public ConfiguredVariableItem(String name, String description, Date value) {
        this(name, description, value, Type.TIME_STAMP);
    }

    public boolean isValid() {
        return strategy.isValue(getValue());
    }

    /**
     * To change the value of variable
     *
     * @param <T> type of value
     * @param value new value
     */
    public <T> void set(T value) {
        Objects.requireNonNull(value);
        setValue(strategy.toString(value));
    }

    /**
     * To get the value of particular type
     *
     * @param <T> possible type of value
     * @param resultType the type of result (class)
     * @return the value by type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> resultType) {
        return strategy.toValue(resultType, getValue());
    }

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getDescription();

    public abstract void setDescription(String description);

    public abstract String getValue();

    public abstract void setValue(String value);

    public abstract Type getType();

    public abstract void setType(Type type);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ConfiguredVariableItem)) return false;
        ConfiguredVariableItem that = (ConfiguredVariableItem) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getValue(), that.getValue()) &&
                Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getValue(), getType());
    }


    // inner classes
    public enum Type {
        STRING, INTEGER, DOUBLE, TIME_STAMP
//        ,XML, JSON
    }
}
