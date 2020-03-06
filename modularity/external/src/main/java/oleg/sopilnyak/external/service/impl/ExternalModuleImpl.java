/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.external.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.external.dto.MetricContainerDto;
import oleg.sopilnyak.external.dto.ModuleValuesDto;
import oleg.sopilnyak.external.service.ExternalModule;
import oleg.sopilnyak.module.ModuleValues;
import oleg.sopilnyak.service.model.dto.ModuleDto;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Type - external service's module realization
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"moduleChecker"})
public class ExternalModuleImpl extends ModuleDto implements ExternalModule {
	// module values by hosts
	private Map<String, ModuleValuesDto> moduleValues;
	// last touched time
	private long touched = -1;
	// host where module is registered
	private String registeredIn;
	// flag is module started
	private boolean active;
	// common module's metrics container for all hosts
	private MetricContainerDto metricsContainer;
	@JsonIgnore
	private transient ExternalModuleChecker moduleChecker;

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
			registeredIn = null;
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
	 * To walk through values of the module
	 *
	 * @param visitor instance to visit each module's values
	 */
	@Override
	public void accept(ModuleValues.Visitor visitor) {
		this.touched = System.currentTimeMillis();
		moduleValues.values().forEach(values -> visitor.visit(values));
	}

	/**
	 * To refresh module's state before return from registry
	 *
	 * @return true if registered
	 */
	@Override
	public boolean isModuleRegistered() {
		return Objects.isNull(moduleChecker) ? false : moduleChecker.isValidModule(this);
	}

	/**
	 * To get values for external host (registered previously)
	 *
	 * @param host the host where module is working
	 * @return values or null if not registered
	 */
	@Override
	public ModuleValues valuesFor(String host) {
		return moduleValues.get(host);
	}

	/**
	 * To check is module didn't touched during expired duration
	 *
	 * @param moduleExpiredDuration expired duration
	 * @return true if module didn't touched during expired duration
	 */
	@Override
	public boolean isExpired(long moduleExpiredDuration) {
		return touched < 0L ? true : touched + moduleExpiredDuration < System.currentTimeMillis();
	}

	/**
	 * The size of registered values
	 *
	 * @return the amount of registered module's values by hosts
	 */
	@Override
	public boolean hasValues() {
		return !CollectionUtils.isEmpty(moduleValues);
	}

	/**
	 * To get host for which external module was registered
	 *
	 * @param registryHost module's host
	 */
	@Override
	public void registryIn(String registryHost) {
		this.registeredIn = registryHost;
	}

	/**
	 * To get the host where external module is registered for synchronization
	 *
	 * @return registry host
	 */
	@Override
	public String registryIn() {
		return registeredIn;
	}

	/**
	 * To register module's values for further processing
	 *
	 * @param values values to register
	 * @return true if success
	 */
	@Override
	public boolean registerValues(ModuleValuesDto values) {
		assert values != null;
		if (moduleValues == null) {
			moduleValues = new LinkedHashMap<>();
		}
		return Objects.isNull(moduleValues.putIfAbsent(values.getHost(), values));
	}

	/**
	 * To un-register values from the module
	 *
	 * @param values registered values
	 * @return true if success
	 */
	@Override
	public boolean unRegisterValues(ModuleValues values) {
		assert values != null;
		return Objects.isNull(moduleValues) ? false : Objects.nonNull(moduleValues.remove(values.getHost()));
	}

	// private methods
	void clearValues() {
		if (moduleValues == null) {
			moduleValues = new LinkedHashMap<>();
		}
		moduleValues.clear();
	}
}
