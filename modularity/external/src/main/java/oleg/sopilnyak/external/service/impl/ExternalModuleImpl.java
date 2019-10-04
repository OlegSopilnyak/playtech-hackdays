/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.external.dto.MetricContainerDto;
import oleg.sopilnyak.external.dto.ModuleValuesDto;
import oleg.sopilnyak.external.service.ExternalModule;
import oleg.sopilnyak.module.ModuleValues;
import oleg.sopilnyak.service.model.dto.ModuleDto;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Type - external service's module realization
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalModuleImpl extends ModuleDto implements ExternalModule {
	// module values by hosts
	private Map<String, ModuleValuesDto> moduleValues;
	private long touched = -1;
	private String registeredFor;
	//	private ModuleAction mainAction;
	private boolean active;
	//	private ModuleHealthCondition condition;
//	private Map<String, VariableItem> configuration;
//	private Map<String, VariableItemDto> changed;
	private MetricContainerDto metricsContainer;
//	private transient Map<String, ExternalModule> sharedModulesMap;

	/**
	 * To start module activity
	 */
	@Override
	public void moduleStart() {
		if (!active) {
			active = true;
			touched = System.currentTimeMillis();
			log.debug("Staring module '{}'", primaryKey());
		}
	}

	/**
	 * To stop module activity
	 */
	@Override
	public void moduleStop() {
		if (active) {
			active = false;
			log.debug("Stopping module '{}'", primaryKey());
		}
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
	 * To return stream of module's values
	 *
	 * @return stream
	 */
	@Override
	public Stream<ModuleValues> values() {
		this.touched = System.currentTimeMillis();
		return moduleValues.values().stream().map(v -> (ModuleValues) v);
	}

	/**
	 * After action detected fail
	 *
	 * @param exception cause of fail
	 */
//	@Override
//	public void healthGoDown(Throwable exception) {
//		throw new UnsupportedOperationException("Not supported for external module.");
//	}

	/**
	 * To get instance of last thrown exception
	 *
	 * @return exception or null if wouldn't
	 */
//	@Override
//	public Throwable lastThrown() {
//		throw new UnsupportedOperationException("Not supported for external module.");
//	}

	/**
	 * After action detected success
	 */
//	@Override
//	public void healthGoUp() {
//		throw new UnsupportedOperationException("Not supported for external module.");
//	}

	/**
	 * To get access to Module's metrics container
	 *
	 * @return instance
	 */
//	@Override
//	public MetricsContainer getMetricsContainer() {
//		return metrics;
//	}

	/**
	 * Notification about change configuration
	 *
	 * @param changed map with changes
	 */
//	@Override
//	public void configurationChanged(Map<String, VariableItem> changed) {
////		this.changed = changed;
//	}

	/**
	 * To refresh module's state before return from registry
	 */
	@Override
	public void refreshModuleState() {
//		if (Objects.isNull(sharedModulesMap)) {
//			log.debug("Not registered locally module.");
//			return;
//		}
//		final ExternalModule shared;
//		final String modulePK = this.primaryKey();
//		if (Objects.nonNull(shared = sharedModulesMap.get(modulePK))) {
//			log.debug("Copy fresh data from distributed map for '{}'", modulePK);
//			final ExternalModuleImpl external = this;
//			ModuleMapper.INSTANCE.copyExternalModule(external, shared);
//			metrics.setMetrics(shared.metrics());
//		} else {
//			log.debug("Module '{}' is not exists in distributed map", modulePK);
//			active = false;
//			condition = ModuleHealthCondition.DAMAGED;
//		}
	}

	/**
	 * To get values for external host (registered previously)
	 *
	 * @param host the host where module is working
	 * @return values or null if not registered
	 */
	@Override
	public ModuleValues getValuesFor(String host) {
		return moduleValues.get(host);
	}

	/**
	 * The size of registered values
	 *
	 * @return the amount of registered module's values by hosts
	 */
	@Override
	public int valuesSize() {
		return moduleValues == null ? 0 : moduleValues.size();
	}

	/**
	 * To get host for which external module was registered
	 *
	 * @param moduleHost module's host
	 */
	@Override
	public void registeredFor(String moduleHost) {
		this.registeredFor = moduleHost;
	}

	/**
	 * To register module's values for further processing
	 *
	 * @param values values to register
	 * @return true if success
	 */
	@Override
	public boolean registerValues(ModuleValuesDto values) {
		return Objects.isNull(moduleValues.putIfAbsent(values.getHost(), values));
	}


	/**
	 * To merge main configuration with changed
	 */
//	@Override
	public void repairConfiguration() {
//		configuration = new LinkedHashMap<>(configuration);
//		configuration.putAll(changed);
//		changed = Collections.EMPTY_MAP;
	}
}
