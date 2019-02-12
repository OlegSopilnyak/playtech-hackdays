/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.registry;

import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.ModuleServiceAdapter;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service: adapter for iterate registered modules
 */
public abstract class RegistryModulesIteratorAdapter extends ModuleServiceAdapter {
	/**
	 * Iterate registered modules
	 *
	 * @param label label of modules' processing
	 */
	protected void iterateRegisteredModules(String label) {
		final ModuleAction action = actionsFactory.currentAction();
		final Instant mark = timeService.now();
		final AtomicInteger counter = new AtomicInteger(0);

		registry.registered().stream()
				.filter(module -> isActive())
				.peek(module -> counter.incrementAndGet())
				.forEach(module -> inspectModule(label, action, module));

		// save metric of total modules configuration duration
		getMetricsContainer().duration().total(label, action, timeService.now(), counter.get(), timeService.duration(mark));
	}

	/**
	 * To inspect one module in context of action
	 *
	 * @param label  label of module's processing
	 * @param action action owner of inspection
	 * @param module module to be inspected
	 */
	protected abstract void inspectModule(String label, ModuleAction action, Module module);
}
