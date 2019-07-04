/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.storage.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.storage.ConfigurationStorageRepository;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Realization of storage though configurations repository
 */
@Slf4j
public class ModuleConfigurationStorageImpl  implements ModuleConfigurationStorage {
	public static final int IDLE_DELAY = 200;
	// the future of scanner's runner
	volatile ScheduledFuture runnerFuture;

	// the registry of modules
	@Autowired
	private ModulesRegistryService registry;
	// distributed map for modules' configurations
	@Autowired
	@Qualifier("modules-configuration-map")
	private Map<String, Map<String, VariableItem>> sharedCache;

	// distributed queue for module configuration updates
	@Autowired
	@Qualifier("modules-configuration-queue")
	private BlockingQueue<ConfigurationStorageEvent> sharedQueue;

	// launcher of module's actions
	@Autowired
	private ScheduledExecutorService activityRunner;

	// repository for trivial operations
	@Autowired
	private ConfigurationStorageRepository repository;

	// listeners of configuration change
	final Set<ConfigurationListener> listeners = new LinkedHashSet<>();
	// lock for listeners set
	private final ReadWriteLock listenersLock = new ReentrantReadWriteLock();

	@PostConstruct
	public void initStorage() {
		if (sharedCache.isEmpty()){
			registry.registered().stream().map(m-> new ModuleDto(m)).forEach(m->{
				log.info("Sharing configuration of {}", m.primaryKey());
				sharedCache.putIfAbsent(m.primaryKey(), repository.getConfiguration(m));
			});
		}
		runnerFuture = activityRunner.schedule(this::processConfigurationEvents, 100, TimeUnit.MILLISECONDS);
		log.info("Initiated scan process.");
	}

	@PreDestroy
	public void destroyStorage(){
		if (Objects.nonNull(runnerFuture)){
			log.info("Destroying Storage...");
			if (!runnerFuture.isDone()) {
				runnerFuture.cancel(true);
			}
		}
		runnerFuture = null;
	}
	/**
	 * To get updated configured variables
	 *
	 * @param module  the consumer of configuration
	 * @param current current state of configuration
	 * @return updated variables (emptyMap if none)
	 */
	@Override
	public Map<String, VariableItem> getUpdatedVariables(Module module, Map<String, VariableItem> current) {
		log.debug("Getting configuration's updates for {} former are {}", module.primaryKey(), current);
		// cached (actual) configuration of the module
		final Map<String, VariableItem> cached = sharedCache.computeIfAbsent(module.primaryKey(), m -> new LinkedHashMap<>());
		// variables which not exists in the cache
		final Map<String, VariableItem> notCached = new LinkedHashMap<>();
		// variables which value is different with cached variable (returns cached value)
		final Map<String, VariableItem> updated = new LinkedHashMap<>();

		log.debug("Iterating module {} configuration", module.primaryKey());
		// iterating current module's configuration
		current.entrySet().forEach(entry -> {
			final String path = entry.getKey();
			final VariableItem value = entry.getValue(), cachedValue = cached.get(path);
			if (Objects.isNull(cachedValue)) notCached.put(path, value);
			else if (!cachedValue.equals(value)) updated.put(path, cachedValue);
		});

		// save not cached configuration if exists
		if (!notCached.isEmpty()) {
			log.debug("Expand module {} configuration by {}", module.primaryKey(), notCached);
			// put request to queue
			sharedQueue.add(new ExpandConfigurationEvent(module, notCached));
			// add not-cached configurations
			cached.putAll(notCached);
			// put updated configuration back to the distributed(shared) cache
			sharedCache.put(module.primaryKey(), cached);
		}
		return updated;
	}

	/**
	 * To update configuration of module
	 *
	 * @param module        target module
	 * @param configuration new configuration
	 */
	@Override
	public void updateConfiguration(Module module, Map<String, VariableItem> configuration) {
		log.debug("Updating module {} by {}", module.primaryKey(), configuration);
		// put request to queue
		sharedQueue.add(new ReplaceConfigurationEvent(module, configuration));
		// notify all configuration change listeners
		notifyConfigurationListeners(Collections.singleton(module.primaryKey()));
	}

	/**
	 * To add modules configuration change listener
	 *
	 * @param listener listener of changes
	 */
	@Override
	public void addConfigurationListener(ConfigurationListener listener) {
		log.debug("Registering module-configuration-listener {}.", listener);
		listenersLock.writeLock().lock();
		try {
			listeners.add(listener);
		} finally {
			listenersLock.writeLock().unlock();
		}
	}

	/**
	 * To remove modules configuration change listener
	 *
	 * @param listener listener of changes
	 */
	@Override
	public void removeConfigurationListener(ConfigurationListener listener) {
		log.debug("Removing module-configuration-listener {}.", listener);
		listenersLock.writeLock().lock();
		try {
			listeners.remove(listener);
		} finally {
			listenersLock.writeLock().unlock();
		}
	}

	/**
	 * To process events from queue
	 */
	protected void processConfigurationEvents() {
		log.debug("Processing check queue job.");
		while (!sharedQueue.isEmpty()) {
			try {
				final ConfigurationStorageEvent event = sharedQueue.take();
				log.debug("Took event {}", event);
				event.update(repository);
			} catch (Throwable e) {
				log.warn("Cannot process configuration event.", e);
			}
		}
		log.debug("Reschedule check queue job.");
		runnerFuture = activityRunner.schedule(this::processConfigurationEvents, IDLE_DELAY, TimeUnit.MILLISECONDS);
	}

	// private methods
	void notifyConfigurationListeners(Set<String> modules) {
		log.debug("Notifying listeners by {}.", modules);
		listenersLock.readLock().lock();
		try {
			listeners.forEach(l -> activityRunner.execute(() -> l.changedModules(modules)));
		} finally {
			listenersLock.readLock().unlock();
		}
	}
}
