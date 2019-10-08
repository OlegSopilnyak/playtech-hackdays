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
import java.util.stream.Stream;

/**
 * Type - external service's module realization
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"facadeImpl"})
public class ExternalModuleImpl extends ModuleDto implements ExternalModule {
	// module values by hosts
	private Map<String, ModuleValuesDto> moduleValues;
	private long touched = -1;
	private String registeredIn;
	//	private ModuleAction mainAction;
	private boolean active;
	//	private ModuleHealthCondition condition;
//	private Map<String, VariableItem> configuration;
//	private Map<String, VariableItemDto> changed;
	private MetricContainerDto metricsContainer;
	@JsonIgnore
	private transient ModuleSystemFacadeImpl facadeImpl;
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
	 * To refresh module's state before return from registry
	 *
	 * @return true if registered
	 */
	@Override
	public boolean isModuleRegistered() {
		return facadeImpl.isModuleRegistered(this);
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

}
