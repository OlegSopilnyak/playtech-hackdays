/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service.registry.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.ModuleBasics;
import oleg.sopilnyak.module.metric.ModuleMetric;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.RegistryModulesIteratorAdapter;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import oleg.sopilnyak.service.metric.storage.ModuleMetricStorage;
import oleg.sopilnyak.service.model.dto.VariableItemDto;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for register modules and check their registry condition
 */
@Slf4j
public class HealthModuleRegistryServiceImpl extends RegistryModulesIteratorAdapter implements ModulesRegistryService {
	private static final String ACTIVITY_LABEL = "HealthCheck";
	// future of scheduled activities
	private volatile ScheduledFuture runnerFuture;
	private volatile long delay = DELAY_DEFAULT;
	private volatile String[] ignoredModules = IGNORE_MODULE_DEFAULT.split(",");
	// the map of registered modules
	private final Map<String, Module> modules = new ConcurrentHashMap<>();

	@Autowired
	private ModuleMetricStorage metricStorage;
	@Autowired
	private ModuleActionStorage actionStorage;

	public HealthModuleRegistryServiceImpl() {
		super.registry = this;

		final VariableItem delayVariable = new VariableItemDto(DELAY_NAME, DELAY_DEFAULT);
		final VariableItem ignoreModulesVariable = new VariableItemDto(IGNORE_MODULE_NAME, IGNORE_MODULE_DEFAULT);

		moduleConfiguration.put(delayName(), delayVariable);
		moduleConfiguration.put(ignoreModulesName(), ignoreModulesVariable);
	}

	/**
	 * To add module to registry
	 *
	 * @param module to add
	 */
	@Override
	public void add(final Module module) {
		assert module != null;
		if (module != this) {
			// activate main module action
			activateMainModuleAction();

			log.debug("Registering '{}'", module.primaryKey());
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
		assert module != null;
		if (module != this) {
			// activate main module action
			activateMainModuleAction();

			log.debug("Unregistering '{}'", module.primaryKey());
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
		return modules.values().stream()
				.peek(m->m.refreshModuleState())
				.collect(Collectors.toCollection(LinkedList::new));
	}

	/**
	 * To get registered module by module's primary key
	 *
	 * @param modulePK primary key
	 * @return module or null if not registered
	 */
	@Override
	public Module getRegistered(String modulePK) {
		return StringUtils.isEmpty(modulePK) ? null : modules.get(modulePK);
	}

	/**
	 * To get registered module by module instance
	 *
	 * @param module module instance
	 * @return module or null if not registered
	 */
	@Override
	public Module getRegistered(ModuleBasics module) {
		assert module != null : "Module couldn't be null";
		return getRegistered(module.primaryKey());
	}

	/**
	 * Allocate module's resources and get module ready to work
	 */
	@Override
	protected void initAsService() {
		if (log.isDebugEnabled()) {
			log.debug("Initiating service...");
		}
		final Module previous = modules.putIfAbsent(this.primaryKey(), this);
		if (log.isDebugEnabled()) {
			log.debug("Registered '{}' is '{}'", this.primaryKey(), Objects.isNull(previous));
		}
		runnerFuture = activityRunner.schedule(this::scanModulesHealth, 500, TimeUnit.MILLISECONDS);
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
		log.debug("Unregistering module '{}'", this.primaryKey());
		modules.remove(this.primaryKey());
	}

	/**
	 * Notify about changes of configuration property
	 *
	 * @param itemName  name of property
	 * @param itemValue new value of property
	 */
	@Override
	protected boolean configurationItemChanged(String itemName, VariableItem itemValue) {
		final VariableItem configurationVariable;
		if (Objects.isNull(configurationVariable = configurationVariableOf(itemName))) {
			log.warn("No accessible variable '{}' in configuration.", itemName);
			return false;
		}
		// check for delay
		if (itemName.equals(delayName())) {
			delay = itemValue.get(Integer.class).longValue();
			log.debug("Changed variable 'delay' to {}", delay);
			configurationVariable.set(delay);
			return true;
		}
		// check for ignoredModules
		if (itemName.equals(ignoreModulesName())) {
			ignoredModules = itemValue.get(String.class).split(",");
			log.debug("Changed variable 'ignoredModules' to {}", Arrays.asList(ignoredModules));
			configurationVariable.set(String.join(",", ignoredModules));
			return true;
		}
		return false;
	}

	/**
	 * To inspect one module in context of action
	 *
	 * @param label  label of module's processing
	 * @param action action owner of inspection
	 * @param module module to be inspected
	 */
	protected void inspectModule(String label, ModuleAction action, Module module) {
		final Instant mark = timeService.now();
		final String modulePK = module.primaryKey();
		if (Stream.of(ignoredModules).anyMatch(ignorance -> !StringUtils.isEmpty(ignorance) && modulePK.startsWith(ignorance))) {
			log.debug("Module '{}' is ignored for inspection.", modulePK);
			return;
		}
		log.info("Scan module '{}'", modulePK);
		// get health state of module
		module.getMetricsContainer().health().heartBeat(action, module);
		// collect and save all metrics of module
		AtomicInteger counter = new AtomicInteger(1);
		module.metrics().stream()
				.peek(metric -> log.debug("{}. metric '{}' processing", counter.getAndIncrement(), metric.getName()))
				.filter(metric -> this.isActive())
				.forEach(this::storeMetric);

		// save metric about module health check duration
		getMetricsContainer().duration().simple(label, action, timeService.now(), modulePK, timeService.duration(mark));
	}


	// private methods

	/**
	 * To scan modules registry
	 */
	void scanModulesHealth() {
		// activate main module action
		activateMainModuleAction();

		log.debug("Scanning modules.");
		actionsFactory.executeAtomicModuleAction(this, "metrics-check", () -> iterateRegisteredModules(ACTIVITY_LABEL), false);

		if (!isActive() || Objects.isNull(runnerFuture)) {
			log.debug("Scanning is stopped.");
			return;
		}
		log.debug("Rescheduling service for {} millis", delay);
		runnerFuture = activityRunner.schedule(this::scanModulesHealth, delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * To store metric into metrics storage
	 *
	 * @param metric metric to be saved
	 */
	void storeMetric(ModuleMetric metric) {
		log.debug("Storing metric '{}' of '{}'", metric.getName(), metric.getAction().getModule().primaryKey());
		final ModuleAction action = metric.getAction();
		actionStorage.persist(action);

		final String modulePK = action.getModule().primaryKey();
		final String metricName = metric.getName();
		final Instant metricMeasured = metric.getMeasured();
		final String metricHost = action.getHostName();
		final String metricActionId = action.getId();
		final String metricData = metric.valuesAsString();
		metricStorage.storeMetric(metricName, modulePK, metricMeasured, metricHost, metricActionId, metricData);
		log.debug("Stored '{}'", metric);
	}
}
