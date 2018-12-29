/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module;

import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;

import java.util.Collection;

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
	 * To get the health condition of module for the moment
	 *
	 * @return current condition value
	 */
	ModuleHealthCondition getCondition();

	/**
	 * After action detected fail
	 */
	void healthGoLow();

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
		final Collection<ModuleMetric> snapshot = getMetricsContainer().metrics();
		getMetricsContainer().clear();
		return snapshot;
	}

}
