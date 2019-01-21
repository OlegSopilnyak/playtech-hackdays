/**
 * Copyright (C) Oleg Sopilnyak 2018
 */
package oleg.sopilnyak.service;


import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.metric.MetricsContainer;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.ModuleHealthCondition;
import oleg.sopilnyak.module.model.VariableItem;
import oleg.sopilnyak.module.model.action.ModuleActionAdapter;
import oleg.sopilnyak.service.action.ModuleActionFactory;
import oleg.sopilnyak.service.registry.ModulesRegistry;
import org.springframework.beans.factory.annotation.Autowired;

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
	protected ModulesRegistry registry;
	// the factory of actions
	@Autowired
	protected ModuleActionFactory actionsFactory;
	// container of metrics
	@Autowired
	protected MetricsContainer metricsContainer;
	// service of time
	@Autowired
	protected TimeService timeService;
	// lock for access to mainAction
	private final Lock mainActionLock = new ReentrantLock();


	/**
	 * Action after module is built
	 */
	public void initialSetUp() {
		if (active) {
			return;
		}
		// start main-action activity
		activateMainModuleAction();

		healthCondition = INIT;
		// register module in registry
		if (this instanceof ModulesRegistry){
			// used other technic for registration
		}else {
			registry.add(this);
		}

		// concrete-module-related init
		ModuleAction result = actionsFactory.executeAtomicModuleAction(this, "init-module", ()->initAsService(), false);

		final ModuleActionAdapter mainAction = (ModuleActionAdapter) getMainAction();
		if (result.getState() == ModuleAction.State.FAIL){
			metricsContainer.action().fail(mainAction, lastThrow);
		}else {
			// module is active
			active = true;
			// running main action
			mainAction.setState(ModuleAction.State.PROGRESS);
			metricsContainer.action().changed(mainAction);
		}
	}

	/**
	 * Actions before module shut down
	 */
	public void shutdownModule() {
		if (!active) {
			return;
		}

		// start main-action activity
		activateMainModuleAction();

		active = false;
		if (this instanceof ModulesRegistry){
			// used other technic for registration
		}else {
			registry.remove(this);
		}


		// concrete-module-related shutdown
		actionsFactory.executeAtomicModuleAction(this, "shutdown-module", ()->shutdownAsService(), false);

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
			}finally {
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
					shutdownModule();
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
	 * To restart module
	 */
	@Override
	public void restart() {
		if (!canRestart()) {
			return;
		}
		if (isActive()) {
			shutdownModule();
		}
		initialSetUp();
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

		changed.forEach((k, v) -> configurationItemChanged(k, v));

		restart();
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

	// protected methods - should be redefined in children
	protected void activateMainModuleAction(){
		// setup action for main-module activity
		actionsFactory.startMainAction(this);
	}

	protected void finishModuleAction(boolean success){
		actionsFactory.finishMainAction(this , success);
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
	 */
	protected void configurationItemChanged(String itemName, VariableItem itemValue) {
	}

}
