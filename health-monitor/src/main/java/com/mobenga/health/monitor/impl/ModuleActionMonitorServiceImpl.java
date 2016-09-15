package com.mobenga.health.monitor.impl;

import com.hazelcast.core.HazelcastInstance;
import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.MonitoredAction;
import com.mobenga.health.monitor.ModuleMonitoringService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.storage.MonitoredActionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * realization of core monitoring service
 * @see ModuleMonitoringService
 */
public class ModuleActionMonitorServiceImpl implements ModuleMonitoringService, MonitoredService {
    private static final Logger LOG = LoggerFactory.getLogger(ModuleActionMonitorServiceImpl.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private MonitoredActionStorage storage;

    @Autowired
    private ModuleStateNotificationService notifier;

    private final Map<String, ConfiguredVariableItem> config = new HashMap<>();

    public void initialize(){
        notifier.register(this);
    }
    public void shutdown(){
        notifier.unRegister(this);
    }
    /**
     * To create the instance of MonitoredAction.class
     *
     * @return the instance doesn't attached to database
     */
    @Override
    public MonitoredAction createMonitoredAction() {
        LOG.debug("Creating MonitoredAction instance");
        return storage.createMonitoredAction();
    }

    /**
     * To monitoring the action proceed in the system
     *
     * @param application the owner of action
     * @param action      monitored action bean
     */
    @Override
    public void actionMonitoring(HealthItemPK application, MonitoredAction action) {
        LOG.debug("Saving MonitoredAction '{}' for '{}'", new Object[]{application, action});
        storage.saveActionState(application, action);
    }

    /**
     * The handle to restart monitored service
     */
    @Override
    public void restart() {
        LOG.info("Restarting...");
    }

    /**
     * To get current configuration of module
     *
     * @return the map
     */
    @Override
    public Map<String, ConfiguredVariableItem> getConfiguration() {
        return config;
    }

    /**
     * Notification about change configuration
     *
     * @param changed map with changes
     */
    @Override
    public void configurationChanged(Map<String, ConfiguredVariableItem> changed) {

    }

    /**
     * to get the value of item's system
     *
     * @return the value
     */
    @Override
    public String getSystemId() {
        return "healthMonitor";
    }

    /**
     * to get the value of item's application
     *
     * @return the value
     */
    @Override
    public String getApplicationId() {
        return "monitoredActionsService";
    }

    /**
     * to get the value of item's application version
     *
     * @return the value
     */
    @Override
    public String getVersionId() {
        return "0.1";
    }

    /**
     * to get description of module
     *
     * @return the value
     */
    @Override
    public String getDescription() {
        return "The service to monitor actions of the module";
    }

    /**
     * To get the value of Module's PK
     *
     * @return value of PK (not null)
     */
    @Override
    public HealthItemPK getModulePK() {
        return this;
    }

    /**
     * Describe the state of module
     *
     * @return true if module active
     */
    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String toString() {
        return "-ModuleActionMonitorService-";
    }
}
