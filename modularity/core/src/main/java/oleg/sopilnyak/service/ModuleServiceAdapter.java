/*
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service;


import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.module.storage.ModuleStorage;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.action.bean.result.ResultModuleAction;
import oleg.sopilnyak.service.configuration.storage.ModuleConfigurationStorage;
import oleg.sopilnyak.service.registry.ModulesRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static oleg.sopilnyak.module.model.ModuleHealthCondition.*;

/**
 * Adapter for periodical actions service
 */
public abstract class ModuleServiceAdapter implements ServiceModule {
	public static final String INIT_MODULE_ACTION_NAME = "init-module";
	public static final String SHUTDOWN_MODULE_ACTION_NAME = "shutdown-module";
	public static final String CONFIGURE_MODULE_ACTION_NAME = "configure-module";

	// main action of module
	private volatile ModuleAction moduleMainAction;

	// the lock for module's health-condition changes
	private final Lock healthLock = new ReentrantLock(true);

	protected volatile boolean active = false;
	protected volatile ModuleHealthCondition healthCondition;

	// last throwable value
	protected volatile Throwable lastThrow;

	// current module's configuration map
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
	// the storage of registered module
	@Autowired
	private ModuleStorage moduleStorage;

	// lock for access to mainAction
	private final Lock mainActionLock = new ReentrantLock(true);


	/**
	 * Action after module is built
	 */
	@Override
	public void moduleStart() {
		if (active) {
			return;
		}

		this.healthCondition = INIT;
		// register module in registry
		if (!(this instanceof ModulesRegistryService)) {
			registry.register(this);
		}
		// save new state of module to the storage
		moduleStorage.saveHealthState(this, this);

		// start main-action activity
		final ModuleAction mainAction;
		startingModuleService(mainAction = getMainAction());

		if (active) {
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
		moduleStorage.removeHealthState(this, this);

		// saving last condition value to temp value & lastThrow
		final ModuleHealthCondition lastCondition = this.healthCondition;
		final Throwable lastThrow = this.lastThrow;

		// concrete-module-related shutdown
		executeAtomicAction(SHUTDOWN_MODULE_ACTION_NAME, this::shutdownAsService);


		// finish main-action activity
		this.lastThrow = lastThrow;
		this.healthCondition = lastCondition;
		shutdownMainModuleAction(healthCondition != FAIL);
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
	public boolean isWorking() {
		return active;
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
			moduleStorage.saveHealthState(this, this);
			healthLock.unlock();
		}
	}

	/**
	 * After action detected fail
	 */
	@Override
	public void healthGoDown(Throwable exception) {
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
			moduleStorage.saveHealthState(this, this);
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

		final AtomicBoolean needModuleRestart = new AtomicBoolean(false);
		changed.forEach((k, v) -> needModuleRestart.getAndSet(needModuleRestart.get() || configurationItemChanged(k, v)));

		restart(needModuleRestart.get());
	}

	/**
	 * To get the host where module is working
	 *
	 * @return the value
	 */
	@Override
	public String getHost() {
		return actionsFactory.getHost();
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
	public VariableItem configurationVariableOf(String varName) {
		return !isWorking() || StringUtils.isEmpty(varName) ? null : getConfiguration().get(varName);
	}

	// protected methods - should be redefined in children

	/**
	 * To activate main action of the service
	 */
	protected void activateMainModuleAction() {
		actionsFactory.startMainAction(this);
	}

	/**
	 * To finish service activities
	 *
	 * @param success flag how it should be finished
	 */
	protected void shutdownMainModuleAction(boolean success) {
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
	ResultModuleAction executeAtomicAction(String actionName, Runnable toRun) {
		return (ResultModuleAction) actionsFactory.executeAtomicModuleAction(this, actionName, toRun, false);
	}

	void setupModuleConfiguration() {
		final Instant mark = timeService.now();

		configurationStorage.getUpdatedVariables(this, getConfiguration()).forEach((k, v) -> this.configurationItemChanged(k, v));

		final ModuleAction action = actionsFactory.currentAction();
		final long operationDuration = timeService.duration(mark);
		getMetricsContainer().duration().simple("Init Configuration", action, timeService.now(), this.primaryKey(), operationDuration);
	}

	void startingModuleService(ModuleAction mainAction) {
		// prepare main-action for the work
		activateMainModuleAction();
		// pretend the module is active
		active = true;

		// setup module's configuration
		{
			final ResultModuleAction result = executeAtomicAction(CONFIGURE_MODULE_ACTION_NAME, this::setupModuleConfiguration);
			if (result.getState() == ModuleAction.State.FAIL) {
				metricsContainer.action().fail(mainAction, result.getCause());
				// module couldn't configured properly
				active = false;
				return;
			}
		}

		// concrete-module-related init as service
		{
			final ResultModuleAction result = executeAtomicAction(INIT_MODULE_ACTION_NAME, this::initAsService);

			if (result.getState() == ModuleAction.State.FAIL) {
				metricsContainer.action().fail(mainAction, result.getCause());
				// module couldn't initialize as service properly
				active = false;
				return;
			}
		}
	}
}
