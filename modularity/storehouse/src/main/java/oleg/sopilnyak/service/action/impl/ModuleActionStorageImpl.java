/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.action.ModuleActionsRepository;
import oleg.sopilnyak.service.action.bean.ActionMapper;
import oleg.sopilnyak.service.action.bean.factory.ModuleMainAction;
import oleg.sopilnyak.service.action.bean.factory.ModuleRegularAction;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Service-implementation: storage of module's actions
 *
 * @see ModuleActionStorage
 */
@Slf4j
public class ModuleActionStorageImpl implements ModuleActionStorage {

	@Autowired
	private UniqueIdGenerator idGenerator;
	@Autowired
	private ModuleActionsRepository repository;

	String hostName;

	public void setUp() {
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.warn("Cannot get network properties.");
			hostName = "localhost";
		}
	}

	/**
	 * To create and save main-action for module
	 *
	 * @param module owner of action
	 * @return new instance
	 */
	@Override
	public ModuleAction createActionFor(final Module module) {
		log.debug("Creating action for module {}", module.primaryKey());
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
	public ModuleAction createActionFor(final Module module, final ModuleAction parent, final String name) {
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
	public void persist(final ModuleAction action) {
		log.debug("Persisting action {}", action);
		repository.persist(action);
	}

	/**
	 * To get stored action by ID
	 *
	 * @param actionId id of action
	 * @return instance or null if not exists
	 */
	@Override
	public ModuleAction getById(final String actionId) {
		log.debug("Getting action by id {}", actionId);
		return repository.getById(actionId);
	}
}
