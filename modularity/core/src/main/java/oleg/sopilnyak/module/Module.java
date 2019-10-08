/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module;

import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.ModuleMetric;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Type - service's module
 */
public interface Module extends ModuleBasics {
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
	default boolean isWorking() {
		return values().filter(v -> v.isActive()).map(v -> v.isActive()).findFirst().orElse(false);
	}

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
		if (isWorking()) {
			moduleStop();
		}
		moduleStart();
	}

	/**
	 * To return stream of module's values
	 *
	 * @return stream
	 */
	Stream<ModuleValues> values();

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
		final MetricsContainer container = getMetricsContainer();
		try {
			return container.metrics().stream().collect(Collectors.toCollection(LinkedList::new));
		}finally {
			container.clear();
		}
	}

	/**
	 * To refresh module's state before return from registry
	 *
	 * @return true if registered
	 */
	boolean isModuleRegistered();
}
