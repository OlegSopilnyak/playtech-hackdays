/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model;

import oleg.sopilnyak.module.ModuleBasics;

import java.time.Instant;

/**
 * Type action of module
 */
public interface ModuleAction {
	/**
	 * To get basic parameters of module - owner of action
	 *
	 * @return reference to module's basic parameters
	 */
	ModuleBasics getModule();

	/**
	 * To get parent of action
	 *
	 * @return reference to parent or null if root
	 */
	ModuleAction getParent();

	/**
	 * To get unique ID of the action
	 *
	 * @return value
	 */
	String getId();

	/**
	 * To get the name of action
	 *
	 * @return value
	 */
	String getName();

	/**
	 * To get the description of action
	 *
	 * @return value
	 */
	String getDescription();

	/**
	 * The host-name where action proceed
	 *
	 * @return the host-name
	 */
	String getHostName();

	/**
	 * To get time when action started
	 *
	 * @return value
	 */
	Instant getStarted();

	/**
	 * To get duration of action's last (milliseconds)
	 *
	 * @return value
	 */
	Long getDuration();

	/**
	 * To get current state of action
	 *
	 * @return value
	 */
	State getState();

	// inner classes
	// available states of action in lifecycle
	enum State{
		INIT, // initialization phase
		PROGRESS, // proceeding phase
		SUCCESS, // action finished well
		FAIL // something bad happeded during action's proceeding
	}
}
