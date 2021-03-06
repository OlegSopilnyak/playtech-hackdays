/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.configuration.ModuleConfigurationService;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.registry.RegistryModulesIteratorAdapter;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service realization of modules configurations
 *
 * @see oleg.sopilnyak.service.configuration.ModuleConfigurationService
 */
@Slf4j
public class ModuleConfigurationServiceImpl extends RegistryModulesIteratorAdapter implements ModuleConfigurationService {
	private static final String ACTIVITY_LABEL = "Configuration";
	// listener of storage's state changes
	private final StorageListener storageListener = new StorageListener();
	// future of scheduled activities
	private volatile ScheduledFuture runnerFuture;
	// future fo config changes scanner
	private volatile ScheduledFuture notifyFuture;

	// queue of updates
	private final BlockingQueue<Collection<String>> storageChangesQueue = new LinkedBlockingQueue<>(1000);

	/**
	 * Allocate module's resources and get module ready to work
	 */
	@Override
	protected void initAsService() {
		log.debug("Initiating service...");
		configurationStorage.addConfigurationListener(storageListener);
		runnerFuture = activityRunner.schedule(() -> scanModulesConfiguration(), 50, TimeUnit.MILLISECONDS);
	}

	/**
	 * Free allocated resources
	 */
	@Override
	protected void shutdownAsService() {
		log.debug("Stopping service...");
		notifyFuture = stopFuture(notifyFuture);
		runnerFuture = stopFuture(runnerFuture);
		configurationStorage.removeConfigurationListener(storageListener);
		registry.remove(this);
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
		log.debug("Scan module {}", modulePK);
		final Map<String, VariableItem> config = configurationStorage.getUpdatedVariables(module, module.getConfiguration());
		if (config.isEmpty()) {
			getMetricsContainer().duration().simple(label, action, timeService.now(), modulePK, timeService.duration(mark));
			log.debug("Nothing to update properties for {}", modulePK);
			return;
		}
		module.configurationChanged(config);
		getMetricsContainer().duration().simple(label, action, timeService.now(), modulePK, timeService.duration(mark));
		log.debug("Updated module {} by {}", modulePK, config);
	}

	// private methods
	void scanModulesConfiguration() {
		// activate main module action
		activateMainModuleAction();

		log.info("Scanning modules.");
		actionsFactory.executeAtomicModuleAction(this, "configuration-check", () -> iterateRegisteredModules(ACTIVITY_LABEL), false);
	}

	void runNotificationProcessing(Collection<String> modules) {
		storageChangesQueue.offer(modules);
		if (Objects.isNull(notifyFuture) || notifyFuture.isDone()) {
			notifyFuture = activityRunner.schedule(() -> scheduleScan(), 50, TimeUnit.MILLISECONDS);
		} else {
			waitForFutureDone(notifyFuture);
			try {
				storageChangesQueue.take();
			} catch (InterruptedException e) {
				log.debug("Cannot get item from queue.", e);
			}
			notifyFuture = activityRunner.schedule(() -> scheduleScan(), 50, TimeUnit.MILLISECONDS);
		}
	}

	void scheduleScan() {
		// activate main module action
		activateMainModuleAction();

		if (storageChangesQueue.isEmpty()) {
			log.debug("Change configuration queue is empty.");
			return;
		}
		waitForFutureDone(runnerFuture);
		runnerFuture = activityRunner.schedule(() -> scanModulesConfiguration(), 50, TimeUnit.MILLISECONDS);
	}

	void waitForFutureDone(ScheduledFuture future) {
		while (!future.isDone()) {
			try {
				TimeUnit.MILLISECONDS.sleep(200);
			} catch (InterruptedException e) {
				log.error("Insomnia ", e);
			}
		}
	}

	ScheduledFuture stopFuture(ScheduledFuture future) {
		if (Objects.nonNull(future)) {
			future.cancel(true);
		}
		return null;
	}

	// inner classes
	class StorageListener implements ModuleConfigurationStorage.ConfigurationListener {
		/**
		 * Notification about change configuration for modules
		 *
		 * @param modules modules which configuration was changed
		 */
		@Override
		public void changedModules(Collection<String> modules) {
			log.debug("Notification about configuration changes for {}", modules);
			runNotificationProcessing(modules);
		}
	}
}
