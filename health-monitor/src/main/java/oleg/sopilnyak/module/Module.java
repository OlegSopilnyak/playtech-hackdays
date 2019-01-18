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
	 * To check is module active (is working)
	 *
	 * @return true if module is working
	 */
	boolean isActive();

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
	 * After action detected success
	 */
	void healthGoUp();

	/**
	 * To check is module allows to be restarted
	 *
	 * @return true if module can restart
	 */
	boolean canRestart();

	/**
	 * To restart module
	 */
	void restart();

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
		final MetricsContainer metricsContainer = getMetricsContainer();
		final Collection<ModuleMetric> metrics = new LinkedList<>(metricsContainer.metrics());
		metricsContainer.clear();
		return metrics;
	}

}
