package com.mobenga.health.model.transport;

import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.monitor.strategy.VariableTypeStrategies;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * Local (not entity) realization of configured variable item
 */
public final class LocalConfiguredVariableItem extends ConfiguredVariableItem {
    private String name;
    private String description;
    private String value;
    private Type type;

    public LocalConfiguredVariableItem(String name, String description, String value) {
        super(name, description, value);
    }

    public LocalConfiguredVariableItem(String name, String description, Integer value) {
        super(name, description, value);
    }

    public LocalConfiguredVariableItem(String name, String description, Double value) {
        super(name, description, value);
    }

    public LocalConfiguredVariableItem(String name, String description, Date value) {
        super(name, description, value);
    }
    
    public LocalConfiguredVariableItem(final ConfiguredVariableItem item){
        super(item.getName(), item.getDescription(), item.getType());
        if ( !item.isValid() ){
            throw new IllegalArgumentException("item:"+item+" is not valid by value.");
        }
        setValue(item.getValue());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "LocalConfiguredVariableItem{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", value='" + value + '\'' +
                ", type=" + type +
                '}';
    }

    // private methods
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.writeUTF(name);
        out.writeUTF(description);
        out.writeUTF(value);
        out.writeUTF(type.name());
    }

    private void readObject(ObjectInputStream in) throws IOException
    {
        name = in.readUTF();
        description = in.readUTF();
        value = in.readUTF();
        type = Type.valueOf(in.readUTF());
        strategy = VariableTypeStrategies.get(type);
    }

}
