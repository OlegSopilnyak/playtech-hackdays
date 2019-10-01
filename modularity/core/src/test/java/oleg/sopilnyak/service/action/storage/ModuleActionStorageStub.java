/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.storage;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.ServiceModule;
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.action.bean.ActionMapper;
import oleg.sopilnyak.service.action.bean.factory.ModuleMainAction;
import oleg.sopilnyak.service.action.bean.factory.ModuleRegularAction;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub for actions storage engine
 * @see oleg.sopilnyak.service.action.storage.ModuleActionStorage
 */
@Slf4j
public class ModuleActionStorageStub implements ModuleActionStorage {
	private final Map<String, ModuleAction> storedActions = new HashMap<>();
	@Autowired
	private UniqueIdGenerator idGenerator;
	private String hostName;

	@PostConstruct
	public void setUp() {
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.warn("Cannot get network properties.");
			hostName = "localhost";
		}
	}

	/**
	 * To get access to current node host-name
	 *
	 * @return host-name
	 */
	@Override
	public String getHostName() {
		return hostName;
	}

	/**
	 * To create and save main-action for module
	 *
	 * @param module owner of action
	 * @return new instance
	 */
	@Override
	public ModuleAction createActionFor(Module module) {
		log.info("Creating main action for {}", module.primaryKey());
		final ModuleMainAction action = ActionMapper.INSTANCE.toMainAction(module, idGenerator, hostName);
		module.getMetricsContainer().action().changed(action);
		log.debug("Created action {}", action);
		return action;
	}

	/**
	 * To create and save regular action for the module
	 *
	 * @param module owner of action
	 * @param parent action parent of new action
	 * @param name   the name of action
	 * @return new instance
	 */
	@Override
	public ModuleAction createActionFor(ServiceModule module, ModuleAction parent, String name) {
		log.info("Creating regular action for {} with name {}", module.primaryKey(), name);
		final ModuleRegularAction action = ActionMapper.INSTANCE.toRegularAction(module, parent, name, idGenerator, hostName);
		module.getMetricsContainer().action().changed(action);
		log.debug("Created action {}", action);
		return action;
	}

	/**
	 * To persist Module's action
	 *
	 * @param action action to store
	 */
	@Override
	public void persist(ModuleAction action) {
		log.debug("Persisting action {}", action);
		storedActions.put(action.getId(), action);
	}

	/**
	 * To get stored action by ID
	 *
	 * @param actionId id of action
	 * @return instance or null if not exists
	 */
	@Override
	public ModuleAction getById(String actionId) {
		log.debug("Getting action by id {}", actionId);
		return storedActions.get(actionId);
	}
}
