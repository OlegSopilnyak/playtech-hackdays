/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.health;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.HealthMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.ModuleServiceAdapter;
import oleg.sopilnyak.service.ModulesRegistry;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for register modules and check their health condition
 */
@Slf4j
public class HealthModuleService extends ModuleServiceAdapter implements ModulesRegistry {
	// the map of registered modules
	private final Map<String, Module> modules = new ConcurrentHashMap<>();
	// future of scheduled activities
	private volatile ScheduledFuture runnerFuture;

	/**
	 * To add module to registry
	 *
	 * @param module to add
	 */
	@Override
	public void add(final Module module) {
		log.debug("Registering {}", module);
		assert module != null;
		if (module != this) {
			modules.putIfAbsent(module.primaryKey(), module);
		}
	}

	/**
	 * To remove module from registry
	 *
	 * @param module to remove
	 */
	@Override
	public void remove(final Module module) {
		log.debug("Unregistering {}", module);
		assert module != null;
		if (module != this) {
			modules.remove(module.primaryKey());
		}
	}

	/**
	 * Allocate module's resources and get module ready to work
	 */
	@Override
	protected void initAsService() {
		log.debug("Initiating service...");
		runnerFuture = activityRunner.scheduleWithFixedDelay(()->scanModulesHealth(), 100, HealthMetric.DELAY, TimeUnit.MILLISECONDS);
	}

	/**
	 * Free allocated resources
	 */
	@Override
	protected void shutdownAsService() {
		log.debug("Stopping service...");
		if (Objects.nonNull(runnerFuture)){
			runnerFuture.cancel(true);
			runnerFuture = null;
		}
	}

	// private methods
	private void scanModulesHealth() {
		final ModuleAction health = actionsFactory.createModuleRegularAction(this, "health-check");
		actionsFactory.executeAtomicModuleAction(health, ()->iterateRegisteredModules(health), false);
	}

	private void iterateRegisteredModules(ModuleAction health) {
		modules.values().forEach(m->inspectModule(health, m));
	}

	private void inspectModule(ModuleAction health, Module module) {

	}
}
