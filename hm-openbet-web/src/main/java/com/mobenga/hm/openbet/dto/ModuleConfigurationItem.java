package com.mobenga.hm.openbet.dto;

import java.io.Serializable;

/**
 * Module configuration item DTO
 */
public class ModuleConfigurationItem implements Serializable{
    // null-item to send back
    public static final ModuleConfigurationItem NULL = new ModuleConfigurationItem("NONE","NONE","NONE", "NULL Item");
    // dot separated name of configuration parameter
    private String path;
    // the type of parameter
    private String type;
    // the value of parameter
    private String value;
    // the description of variable
    private String description;

    public ModuleConfigurationItem() {
    }

    public ModuleConfigurationItem(String path, String type, String value) {
        this.path = path;
        this.type = type;
        this.value = value;
    }

    private ModuleConfigurationItem(String path, String type, String value, String description) {
        this(path, type, value);
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ModuleConfigurationItem{" +
                "path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
