/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.metric;

import oleg.sopilnyak.module.model.ModuleAction;

import java.time.Instant;

public interface DurationMetricsContainer {
	/**
	 * To register simple-duration metric of operation for module in action
	 *
	 * @param label label of activity
	 * @param action action-context
	 * @param measured time of occurrence
	 * @param module pk of processed module
	 * @param duration duration of operation
	 */
	void simple(String label, ModuleAction action, Instant measured, String module, long duration);

	/**
	 * To register total-duration metric of operations for modules in action
	 *
	 * @param label label of activity
	 * @param action action-context
	 * @param measured time of occurrence
	 * @param modules amount of modules
	 * @param duration durations of operations
	 */
	void total(String label, ModuleAction action, Instant measured, int modules, long duration);
}
