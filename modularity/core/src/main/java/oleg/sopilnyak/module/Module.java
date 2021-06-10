/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.module;

import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.metric.ModuleMetric;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

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
		final AtomicBoolean isActive = new AtomicBoolean(false);
		final ModuleValues.Visitor valuesVisitor = values -> {
			if (!isActive.get()){
				isActive.getAndSet(values.isActive());
			}
		};
		accept(valuesVisitor);
		return isActive.get();
	}

	/**
	 * To check is module allows to be restarted
	 *
	 * @return true if module can restart
	 */
	boolean canRestart();

	/**
	 * To restart module
	 *
	 * @param allowedRestart flag is restart allowed
	 */
	default void restart(boolean allowedRestart) {
		if (!allowedRestart || !canRestart()) {
			return;
		}
		if (isWorking()) {
			moduleStop();
		}
		moduleStart();
	}

	/**
	 * To walk through module-values of the module
	 *
	 * @param visitor instance to visit each module's values
	 */
	void accept(ModuleValues.Visitor visitor);

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
			return new LinkedList<>(container.metrics());
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
