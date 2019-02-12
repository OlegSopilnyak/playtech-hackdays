/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module.model;

import oleg.sopilnyak.module.ModuleBasics;

import java.time.Instant;

/**
 * Type to describe state of module
 */
public interface ModuleSnapshot {
	// default delay (milliseconds) between heart-beats
	int DELAY = 2000;

	/**
	 * To get basic parameters of module - owner of snapshot
	 *
	 * @return reference to module's basic parameters
	 */
	ModuleBasics getModule();

	/**
	 * Exact date-time of heard-beat
	 *
	 * @return the time
	 */
	Instant getTime();

	/**
	 * The host-name where heart beats
	 *
	 * @return the host-name
	 */
	String getHostName();
}
