/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module;

import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Type - service's module
 */
public interface Module extends ModuleBasics, ModuleHealth, ModuleConfigurable {
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
		final LinkedList<ModuleMetric> metrics =
				getMetricsContainer().metrics().stream().collect(Collectors.toCollection(LinkedList::new));
		getMetricsContainer().clear();
		return metrics;
	}

	/**
	 * To refresh module's state before return from registry
	 */
	default void refreshModuleState(){

	}
}
