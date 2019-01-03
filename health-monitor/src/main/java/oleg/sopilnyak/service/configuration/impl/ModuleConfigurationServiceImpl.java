/**
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.configuration.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.service.ModuleServiceAdapter;
import oleg.sopilnyak.service.configuration.ModuleConfigurationService;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.springframework.beans.factory.annotation.Autowired;

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
public class ModuleConfigurationServiceImpl extends ModuleServiceAdapter implements ModuleConfigurationService {
	// listener of storage's state changes
	private final StorageListener storageListener = new StorageListener();
	// future of scheduled activities
	private volatile ScheduledFuture runnerFuture;
	// future fo config changes scanner
	private volatile ScheduledFuture notifyFuture;

	// queue of updates
	private final BlockingQueue<Collection<String>> storageChangesQueue = new LinkedBlockingQueue<>(1000);

	// registry of modules
	@Autowired
	private ModulesRegistry registry;
	// active storage of configurations
	@Autowired
	private ModuleConfigurationStorage storage;

	/**
	 * Allocate module's resources and get module ready to work
	 */
	@Override
	protected void initAsService() {
		log.debug("Initiating service...");
		registry.add(this);
		storage.addConfigurationListener(storageListener);
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
		storage.removeConfigurationListener(storageListener);
		registry.remove(this);
	}

	// private methods
	void scanModulesConfiguration() {
		log.info("Scanning modules.");
		final ModuleAction configuration = actionsFactory.createModuleRegularAction(this, "configuration-check");
		actionsFactory.executeAtomicModuleAction(configuration, () -> iterateRegisteredModules(configuration), false);
	}

	void iterateRegisteredModules(ModuleAction configuration) {
		registry.registered().stream().filter(module -> isActive()).forEach(module -> inspectModule(configuration, module));
	}

	void inspectModule(ModuleAction configuration, Module module) {
		final String modulePK = module.primaryKey();
		log.debug("Scan module {}", modulePK);
		final Map<String, VariableItem> config = storage.getUpdatedVariables(module, module.getConfiguration());
		if (config.isEmpty()) {
			log.debug("Nothing to update properties for {}", modulePK);
			return;
		}
		module.configurationChanged(config);
		log.debug("Updated module {} by {}", modulePK, config);
	}

	void runNotificationProcessing(Collection<String> modules) {
		storageChangesQueue.offer(modules);
		if (Objects.isNull(notifyFuture) || notifyFuture.isDone()){
			notifyFuture = activityRunner.schedule(() -> scheduleScan(), 50, TimeUnit.MILLISECONDS);
		} else{
			waitForFutureDone(notifyFuture);
			try {
				storageChangesQueue.take();
			} catch (InterruptedException e) {
				log.debug("Cannot get item from queue.", e);
			}
			notifyFuture = activityRunner.schedule(() -> scheduleScan(), 50, TimeUnit.MILLISECONDS);
		}
	}
	void scheduleScan(){
		if (storageChangesQueue.isEmpty()){
			log.debug("Change configuration queue is empty.");
			return;
		}
		waitForFutureDone(runnerFuture);
		runnerFuture = activityRunner.schedule(() -> scanModulesConfiguration(), 50, TimeUnit.MILLISECONDS);
	}
	void waitForFutureDone(ScheduledFuture future){
		while (!future.isDone()){
			try {
				TimeUnit.MILLISECONDS.sleep(200);
			} catch (InterruptedException e) {
				log.error("Insomnia ", e);
			}
		}
	}
	ScheduledFuture stopFuture(ScheduledFuture future){
		if (Objects.nonNull(future)) {
			future.cancel(true);
		}
		return null;
	}
	// inner classes
	class StorageListener implements ModuleConfigurationStorage.ConfigurationListener{
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