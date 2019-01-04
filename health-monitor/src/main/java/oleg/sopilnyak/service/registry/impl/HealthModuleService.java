/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.registry.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.ModuleServiceAdapter;
import oleg.sopilnyak.service.TimeService;
import oleg.sopilnyak.service.dto.VariableItemDto;
import oleg.sopilnyak.service.metric.HeartBeatMetricContainer;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Service for register modules and check their registry condition
 */
@Slf4j
public class HealthModuleService extends ModuleServiceAdapter implements ModulesRegistry {
	// the map of registered modules
	private final Map<String, Module> modules = new ConcurrentHashMap<>();
	// future of scheduled activities
	private volatile ScheduledFuture runnerFuture;
	private volatile long delay;
	private volatile String[] ignoredModules = new String[0];

	@Autowired
	private ModuleMetricStorage metricStorage;
	@Autowired
	private TimeService timeService;

	public HealthModuleService() {
		moduleConfiguration.put(delayName(), new VariableItemDto(DELAY_NAME, DELAY_DEFAULT));
		moduleConfiguration.put(ignoreModulesName(), new VariableItemDto(IGNORE_MODULE_NAME, IGNORE_MODULE_DEFAULT));
	}

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
	 * To get collection of registered modules
	 *
	 * @return collection of registered modules
	 */
	@Override
	public Collection<Module> registered() {
		return new LinkedHashSet<>(modules.values());
	}

	/**
	 * Allocate module's resources and get module ready to work
	 */
	@Override
	protected void initAsService() {
		log.debug("Initiating service...");
		add(this);
		runnerFuture = activityRunner.schedule(() -> scanModulesHealth(), 50, TimeUnit.MILLISECONDS);
	}

	/**
	 * Free allocated resources
	 */
	@Override
	protected void shutdownAsService() {
		log.debug("Stopping service...");
		if (Objects.nonNull(runnerFuture)) {
			runnerFuture.cancel(true);
			runnerFuture = null;
		}
		remove(this);
	}

	/**
	 * Notify about changes of configuration property
	 *
	 * @param itemName  name of property
	 * @param itemValue new value of property
	 */
	@Override
	protected void configurationItemChanged(String itemName, VariableItem itemValue) {
		if (!isActive() || StringUtils.isEmpty(itemName)) {
			// service is not active or itemName is wrong
			return;
		}
		// check for delay
		if (itemName.equals(delayName())) {
			delay = itemValue.get(Integer.class).longValue();
			log.debug("Changed 'delay' to {}", delay);
			moduleConfiguration.get(itemName).set(delay);
		}
		// check for ignoredModules
		if (itemName.equals(ignoreModulesName())) {
			ignoredModules = itemValue.get(String.class).split(",");
			log.debug("Changed 'ignoredModules' to {}", Arrays.asList(ignoredModules));
			moduleConfiguration.get(itemName).set(String.join(",", ignoredModules));
		}
	}


	// private methods

	/**
	 * To scan modules registry
	 */
	void scanModulesHealth() {
		log.info("Scanning modules.");
		final ModuleAction health = actionsFactory.createModuleRegularAction(this, "metrics-check");
		actionsFactory.executeAtomicModuleAction(health, () -> iterateRegisteredModules(health), false);
		if (!isActive() || Objects.isNull(runnerFuture)) {
			// service is stopped
			return;
		}
		log.debug("Rescheduling service");
		runnerFuture = activityRunner.schedule(() -> scanModulesHealth(), delay, TimeUnit.MILLISECONDS);
	}

	void iterateRegisteredModules(ModuleAction health) {
		final Instant mark = timeService.now();
		final AtomicInteger counter = new AtomicInteger(0);
		registered().stream().filter(m -> isActive()).peek(m -> counter.incrementAndGet()).forEach(m -> inspectModule(health, m));

		// save metric of total modules health check duration
		getMetricsContainer().add(new TotalDurationMetric(health, timeService.now(), counter.get(), timeService.duration(mark)));
	}

	void inspectModule(ModuleAction health, Module module) {
		final Instant mark = timeService.now();
		final String modulePK = module.primaryKey();
		log.debug("Scan module {}", modulePK);
		if (Stream.of(ignoredModules).filter(i -> modulePK.startsWith(i)).count() > 0L) {
			log.debug("Module {} is ignored.", modulePK);
			return;
		}
		((HeartBeatMetricContainer) module.getMetricsContainer()).heatBeat(health, module);
		module.metrics().stream().filter(m -> isActive()).forEach(m -> store(m));

		// save metric about module health check duration
		getMetricsContainer().add(new SimpleDurationMetric(health, timeService.now(), modulePK, timeService.duration(mark)));
	}

	void store(ModuleMetric metric) {
		log.debug("Storing metric {} of {}", metric.name(), metric.action().getModule().primaryKey());
		ModuleAction action = metric.action();
		Module module = (Module) action.getModule();
		metricStorage.storeMetric(metric.name(), module.primaryKey(), metric.measured(), action.getHostName(), metric.valuesAsString());
		log.debug("Stored {}", metric);
	}
}
