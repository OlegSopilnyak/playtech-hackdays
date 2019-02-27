/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service;


import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.module.model.action.ResultModuleAction;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static oleg.sopilnyak.module.model.ModuleHealthCondition.*;

/**
 * Adapter for periodical actions service
 */
public abstract class ModuleServiceAdapter implements Module {
	public static final String INIT_MODULE_ACTION_NAME = "init-module";
	public static final String CONFIGURE_MODULE_ACTION_NAME = "configure-module";
	// main action of module
	private volatile ModuleAction moduleMainAction;

	protected volatile boolean active = false;
	protected volatile ModuleHealthCondition healthCondition;
	protected volatile Throwable lastThrow;
	private Lock healthLock = new ReentrantLock();
	protected final Map<String, VariableItem> moduleConfiguration = new ConcurrentHashMap<>();

	// launcher of module's actions
	@Autowired
	protected ScheduledExecutorService activityRunner;
	// the registry of modules
	@Autowired(required = false)
	protected ModulesRegistryService registry;
	// the factory of actions
	@Autowired
	protected ModuleActionFactory actionsFactory;
	// container of metrics
	@Autowired
	protected MetricsContainer metricsContainer;
	// service of time
	@Autowired
	protected TimeService timeService;
	// storage of all modules configurations
	@Autowired
	protected ModuleConfigurationStorage configurationStorage;
	// lock for access to mainAction
	private final Lock mainActionLock = new ReentrantLock();


	/**
	 * Action after module is built
	 */
	@Override
	public void moduleStart() {
		if (active) {
			return;
		}

		healthCondition = INIT;
		// register module in registry
		if (!(this instanceof ModulesRegistryService)) {
			registry.add(this);
		}

		// start main-action activity
		final ModuleActionAdapter mainAction = (ModuleActionAdapter) getMainAction();
		activateMainModuleAction();
		// module is active
		active = true;

		ResultModuleAction result;
		// setup module's configuration
		result = (ResultModuleAction)actionsFactory
				.executeAtomicModuleAction(this, CONFIGURE_MODULE_ACTION_NAME, this::setupModuleConfiguration, false);
		if (result.getState() == ModuleAction.State.FAIL) {
			metricsContainer.action().fail(mainAction, result.getCause());
			// module couldn't configured properly
			active = false;
			return;
		}

		// concrete-module-related init
		result = (ResultModuleAction) actionsFactory
				.executeAtomicModuleAction(this, INIT_MODULE_ACTION_NAME, this::initAsService, false);

		if (result.getState() == ModuleAction.State.FAIL) {
			metricsContainer.action().fail(mainAction, result.getCause());
			// module couldn't start properly
			active = false;
		} else {
			// running main action
			mainAction.setState(ModuleAction.State.PROGRESS);
			metricsContainer.action().changed(mainAction);
		}
	}

	/**
	 * Actions before module shut down
	 */
	@Override
	public void moduleStop() {
		if (!active) {
			return;
		}

		// start main-action activity
		activateMainModuleAction();

		active = false;
		if (!(this instanceof ModulesRegistryService)) {
			registry.remove(this);
		}


		// concrete-module-related shutdown
		final String actionName = "shutdown-module";
		actionsFactory.executeAtomicModuleAction(this, actionName, this::shutdownAsService, false);

		// finish main-action activity
		finishModuleAction(healthCondition != FAIL);
	}

	/**
	 * To get root action of module
	 *
	 * @return instance
	 */
	@Override
	public ModuleAction getMainAction() {
		if (Objects.isNull(moduleMainAction)) {
			mainActionLock.lock();
			try {
				if (Objects.isNull(moduleMainAction)) {
					moduleMainAction = actionsFactory.createModuleMainAction(this);
				}
			} finally {
				mainActionLock.unlock();
			}
		}
		return moduleMainAction;
	}

	/**
	 * To check is module active (is working)
	 *
	 * @return true if module is working
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	/**
	 * To get the registry condition of module for the moment
	 *
	 * @return current condition value
	 */
	@Override
	public ModuleHealthCondition getCondition() {
		return healthCondition;
	}

	/**
	 * After action detected success
	 */
	@Override
	public void healthGoUp() {
		healthLock.lock();
		try {
			lastThrow = null;
			switch (healthCondition) {
				case INIT:
				case GOOD:
					healthCondition = VERY_GOOD;
					break;
				case AVERAGE:
					healthCondition = GOOD;
					break;
				case POOR:
					healthCondition = AVERAGE;
					break;
				case FAIL:
					healthCondition = POOR;
					break;
			}
		} finally {
			healthLock.unlock();
		}
	}

	/**
	 * After action detected fail
	 */
	@Override
	public void healthGoLow(Throwable exception) {
		healthLock.lock();
		try {
			lastThrow = exception;
			switch (healthCondition) {
				case VERY_GOOD:
					healthCondition = GOOD;
					break;
				case GOOD:
					healthCondition = AVERAGE;
					break;
				case AVERAGE:
					healthCondition = POOR;
					break;
				case POOR:
					healthCondition = FAIL;
					break;
				case FAIL:
					moduleStop();
					break;
			}
		} finally {
			healthLock.unlock();
		}
	}

	/**
	 * To get instance of last thrown exception
	 *
	 * @return exception or nul if wouldn't
	 */
	@Override
	public Throwable lastThrown() {
		return lastThrow;
	}

	/**
	 * To check is module allows to be restarted
	 *
	 * @return true if module can restart
	 */
	@Override
	public boolean canRestart() {
		return true;
	}

	/**
	 * To get current configuration of module
	 *
	 * @return configuration as map
	 */
	@Override
	public Map<String, VariableItem> getConfiguration() {
		return moduleConfiguration;
	}

	/**
	 * Notification about change configuration
	 *
	 * @param changed map with changes
	 */
	@Override
	public void configurationChanged(Map<String, VariableItem> changed) {
		if (changed.isEmpty()) {
			return;
		}
		activateMainModuleAction();

		final boolean[] changedModule = new boolean[]{false};
		changed.forEach((k, v) -> changedModule[0] = changedModule[0] || configurationItemChanged(k, v));

		if (changedModule[0]) {
			restart();
		}
	}

	/**
	 * To get access to Module's metrics container
	 *
	 * @return instance
	 */
	@Override
	public MetricsContainer getMetricsContainer() {
		return metricsContainer;
	}

	/**
	 * To get configuration variable by name
	 *
	 * @param varName variable's name
	 * @return value or null if not exists or module is not active
	 */
	public VariableItem configurationVariableOf(String varName){
		if (!isActive() || StringUtils.isEmpty(varName)) {
			// service is not active or itemName is wrong
			return null;
		}
		return moduleConfiguration.get(varName);
	}
	// protected methods - should be redefined in children
	protected void activateMainModuleAction() {
		// setup action for main-module activity
		actionsFactory.startMainAction(this);
	}

	protected void finishModuleAction(boolean success) {
		actionsFactory.finishMainAction(this, success);
	}

	/**
	 * Allocate module's resources and get module ready to work
	 */
	protected void initAsService() {
	}

	/**
	 * Free allocated resources
	 */
	protected void shutdownAsService() {
	}

	/**
	 * Notify about changes of configuration property
	 *
	 * @param itemName  name of property
	 * @param itemValue new value of property
	 * @return true if made change
	 */
	protected boolean configurationItemChanged(String itemName, VariableItem itemValue) {
		return false;
	}

	// private methods
	void setupModuleConfiguration(){
		final Instant mark = timeService.now();
		final ModuleAction action = actionsFactory.currentAction();
		configurationStorage
				.getUpdatedVariables(this, moduleConfiguration)
				.forEach((k,v)->configurationItemChanged(k, v));
		final String modulePK = primaryKey();
		getMetricsContainer().duration().simple("Init Configuration", action, timeService.now(), modulePK, timeService.duration(mark));
	}
}
