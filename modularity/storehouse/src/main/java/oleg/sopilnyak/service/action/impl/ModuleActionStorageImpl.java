/*
 * Copyright (C) Oleg Sopilnyak 2019
 */
package oleg.sopilnyak.service.action.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.module.Module;
import oleg.sopilnyak.module.model.ModuleAction;
import oleg.sopilnyak.module.model.action.ModuleMainAction;
import oleg.sopilnyak.module.model.action.ModuleRegularAction;
import oleg.sopilnyak.service.UniqueIdGenerator;
import oleg.sopilnyak.service.action.ModuleActionsRepository;
import oleg.sopilnyak.service.action.storage.ModuleActionStorage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

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
	private ObjectProvider<ModuleMainAction> mainActions;
	@Autowired
	private ObjectProvider<ModuleRegularAction> regularActions;
	@Autowired
	private ModuleActionsRepository repository;

	private String hostName;

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
	public ModuleAction createActionFor(Module module) {
		final ModuleMainAction action = mainActions.getObject(module);
		action.setName("[main->" + module.getSystemId() + "->" + module.getModuleId() + "]");
		action.setId(idGenerator.generate());
		action.setHostName(hostName);
		action.setDescription("Main action of " + module.getDescription());
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
	public ModuleAction createActionFor(Module module, ModuleAction parent, String name) {
		final ModuleRegularAction action = regularActions.getObject(module, name);
		action.setParent(Objects.isNull(parent) ? module.getMainAction() : parent);
		action.setId(idGenerator.generate());
		action.setHostName(hostName);
		action.setDescription(name + " action of " + module.getDescription());
		module.getMetricsContainer().action().changed(action);
		return action;
	}

	/**
	 * To persist Module's action
	 *
	 * @param action action to store
	 */
	@Override
	public void persist(ModuleAction action) {
		repository.persist(action);
	}

	/**
	 * To get stored action by ID
	 *
	 * @param actionId id of action
	 * @return instance or null if not exists
	 */
	@Override
	public ModuleAction getById(String actionId) {
		return repository.getById(actionId);
	}
}
