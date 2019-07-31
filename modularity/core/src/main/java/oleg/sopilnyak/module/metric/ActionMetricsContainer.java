/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.module.metric;

import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.action.ActionContext;

/**
 * Service: container of metrics for module (actions)
 *
 * @see oleg.sopilnyak.module.metric.MetricsContainer
 */
public interface ActionMetricsContainer {
	/**
	 * To add metric about action state change
	 *
	 * @param action action
	 */
	void changed(ModuleAction action);

	/**
	 * To add metric about action finish not successfully
	 *
	 * @param action action
	 * @param t      occurred exception
	 */
	void fail(ModuleAction action, Throwable t);

	/**
	 * To add metric about action finish successfully
	 *
	 * @param action action
	 */
	void success(ModuleAction action);

	/**
	 * To save metric about start action with parameters
	 *
	 * @param action regular module-action
	 * @param context input parameters of the action
	 */
	void start(ModuleAction action, ActionContext context);

	/**
	 * To save metric about finish action with parameters
	 *
	 * @param action regular module-action
	 * @param output result of the action
	 */
	void finish(ModuleAction action, Object output);
}
