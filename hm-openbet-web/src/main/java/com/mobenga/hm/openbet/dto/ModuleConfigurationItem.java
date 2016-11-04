package com.mobenga.hm.openbet.dto;

/**
 * Module configuration item
 */
public class ModuleConfigurationItem {
    // dot separated name of configuration parameter
    private String path;
    // the type of parameter
    private String type;
    // the value of parameter
    private String value;

    public ModuleConfigurationItem() {
    }

    public ModuleConfigurationItem(String path, String type, String value) {
        this.path = path;
        this.type = type;
        this.value = value;
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

    @Override
    public String toString() {
        return "ModuleConfigurationItem{" +
                "path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
