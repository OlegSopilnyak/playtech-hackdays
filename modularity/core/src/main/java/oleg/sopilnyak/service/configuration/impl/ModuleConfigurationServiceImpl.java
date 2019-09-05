/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.RegistryModulesIteratorAdapter;
import oleg.sopilnyak.service.configuration.ModuleConfigurationService;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import org.springframework.util.CollectionUtils;

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
	static final String ACTIVITY_LABEL = "Configuration";
	private static final String CONFIGURATION_CHECK = "configuration-check";
	static final String CONFIGURATION_UPDATE = "configuration-update";
	// listener of storage's state changes
	private final StorageListener storageListener = new StorageListener();
	// future of scheduled activities
	private volatile ScheduledFuture runnerFuture;
	// future for config changes scanner
	private volatile ScheduledFuture notifyFuture;

	// queue of updates
	private final BlockingQueue<Collection<String>> storageChangesQueue = new LinkedBlockingQueue<>(1000);

	/**
	 * Allocate module's resources and get module ready to work
	 */
	@Override
	protected void initAsService() {
		if (active) {
			log.debug("Service initiated already...");
			return;
		}
		log.debug("Initiating service...");
		configurationStorage.addConfigurationListener(storageListener);
		// prepare scan module configuration runner
		final Module module = this;
		final String actionName = CONFIGURATION_CHECK;
		final Runnable executable = () -> iterateRegisteredModules(ACTIVITY_LABEL);
		final Runnable scanModulesConfiguration = () -> {
			// activate main module action
			activateMainModuleAction();
			// scanning all allowed modules
			log.info("Scanning modules.");
			actionsFactory.executeAtomicModuleAction(module, actionName, executable, false);
		};
		runnerFuture = activityRunner.schedule(scanModulesConfiguration, 50, TimeUnit.MILLISECONDS);
	}

	/**
	 * Free allocated resources
	 */
	@Override
	protected void shutdownAsService() {
		if (!active) {
			log.debug("Service stopped already...");
			return;
		}
		log.debug("Stopping service...");
		notifyFuture = stopFuture(notifyFuture);
		runnerFuture = stopFuture(runnerFuture);
		configurationStorage.removeConfigurationListener(storageListener);
	}

	/**
	 * To inspect one module in context of action
	 *
	 * @param label  label of module's processing
	 * @param action action owner of inspection
	 * @param module module to be inspected
	 * @see RegistryModulesIteratorAdapter##iterateRegisteredModules(String)
	 */
	protected void inspectModule(String label, ModuleAction action, Module module) {
		final Instant mark = timeService.now();
		final String modulePK = module.primaryKey();
		log.debug("Scan module {}", modulePK);
		final Map<String, VariableItem> config = configurationStorage.getUpdatedVariables(module, module.getConfiguration());
		if (CollectionUtils.isEmpty(config)) {
			getMetricsContainer().duration().simple(label, action, timeService.now(), modulePK, timeService.duration(mark));
			log.debug("Nothing to update properties for {}", modulePK);
			return;
		}
		module.configurationChanged(config);
		getMetricsContainer().duration().simple(label, action, timeService.now(), modulePK, timeService.duration(mark));
		log.debug("Updated module {} by {}", modulePK, config);
	}

	// private methods
	void notifyModuleConfigurationUpdates(String label, String modulePK){
		final Module module = registry.getRegistered(modulePK);
		if (Objects.isNull(module)){
			log.warn("Module '{}' is not registered.", modulePK);
			return;
		}
		final Module service = this;
		final Runnable executable = ()-> inspectModule(ACTIVITY_LABEL, actionsFactory.currentAction(), module);

		log.debug("Updating module '{}' configuration.", modulePK);
		actionsFactory.executeAtomicModuleAction(service, label, executable, false);
	}

	void runNotificationProcessing(final Collection<String> modules) {
		if (CollectionUtils.isEmpty(modules)){
			log.warn("Listener notified by emptu modules set.");
			return;
		}
		final Runnable executeConfigurationUpdates = () -> {
			// activate main module action for current Thread
			activateMainModuleAction();
			final ModuleAction action = actionsFactory.currentAction();

			log.info("Updating modules {}", modules);
			final String label = CONFIGURATION_UPDATE;
			final Instant mark = timeService.now();
			final int counter[] = new int[]{0};
			modules.forEach(pk -> {
				counter[0]++;
				notifyModuleConfigurationUpdates(label, pk);
			});
			// save metric of notified modules update duration
			getMetricsContainer().duration().total(label, action, timeService.now(), counter[0], timeService.duration(mark));
		};
		waitForFutureDone(notifyFuture);
		notifyFuture = activityRunner.schedule(executeConfigurationUpdates, 0L, TimeUnit.MILLISECONDS);
	}

	void waitForFutureDone(ScheduledFuture future) {
		if (Objects.isNull(future) || future.isDone()) {
			return;
		}
		log.debug("Waiting for previous updates will be done...");
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
