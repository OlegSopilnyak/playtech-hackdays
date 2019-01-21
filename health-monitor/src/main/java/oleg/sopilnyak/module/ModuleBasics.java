/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module;

/**
 * Type: basic properties of Module
 */
public interface ModuleBasics {
	/**
	 * To get the value of module's system
	 *
	 * @return the value
	 */
	String getSystemId();

	/**
	 * To get the value of module's ID
	 *
	 * @return the value
	 */
	String getModuleId();

	/**
	 * To get the value of module's version
	 *
	 * @return the value
	 */
	String getVersionId();

	/**
	 * To get description of module
	 *
	 * @return the value
	 */
	String getDescription();

	/**
	 * String representation of module's Primaky Key
	 *
	 * @return value of PK
	 */
	default String primaryKey() {
		return getSystemId() + "::" + getModuleId() + "::" + getVersionId();
	}
}
