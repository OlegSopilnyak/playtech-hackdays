/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module;

import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Type - service's module
 */
public interface Module extends ModuleBasics, ModuleConfigurable {
	/**
	 * To start module activity
	 */
	void moduleStart();

	/**
	 * To stop module activity
	 */
	void moduleStop();

	/**
	 * To check is module active (is working)
	 *
	 * @return true if module is working
	 */
	boolean isActive();

	/**
	 * To check is module allows to be restarted
	 *
	 * @return true if module can restart
	 */
	boolean canRestart();

	/**
	 * To restart module
	 */
	default void restart() {
		if (!canRestart()) {
			return;
		}
		if (isActive()) {
			moduleStop();
		}
		moduleStart();
	}


	/**
	 * To get the registry condition of module for the moment
	 *
	 * @return current condition value
	 */
	ModuleHealthCondition getCondition();

	/**
	 * After action detected fail
	 *
	 * @param exception cause of fail
	 */
	void healthGoLow(Throwable exception);

	/**
	 * To get instance of last thrown exception
	 *
	 * @return exception or nul if wouldn't
	 */
	Throwable lastThrown();

	/**
	 * After action detected success
	 */
	void healthGoUp();

	/**
	 * To get root action of module
	 *
	 * @return instance
	 */
	ModuleAction getMainAction();

	/**
	 * To get access to Module's metrics container
	 *
	 * @return instance
	 */
	MetricsContainer getMetricsContainer();

	/**
	 * To get module's metrics snapshot and clear internal metrics set
	 *
	 * @return metrics snapshot
	 */
	default Collection<ModuleMetric> metrics() {
		final Collection<ModuleMetric> metrics = new LinkedList<>(getMetricsContainer().metrics());
		getMetricsContainer().clear();
		return metrics;
	}

}
