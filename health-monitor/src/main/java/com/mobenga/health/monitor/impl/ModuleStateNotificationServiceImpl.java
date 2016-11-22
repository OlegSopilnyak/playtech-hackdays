package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.*;
import com.mobenga.health.model.factory.TimeService;
import com.mobenga.health.model.factory.impl.ModuleOutputDeviceFactory;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.model.transport.ModuleHealthItem;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.monitor.ModuleMonitoringService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.monitor.behavior.ModuleHealth;
import com.mobenga.health.storage.HeartBeatStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Realization of module state notifier
 *
 * @see ModuleStateNotificationService
 */
public class ModuleStateNotificationServiceImpl implements ModuleStateNotificationService, MonitoredService {
    public static final String PARAMS_PACKAGE = "health.monitor.service.heartbeat";
    private static final ConfiguredVariableItem HB_DELAY =
            new LocalConfiguredVariableItem("delay", "The delay between heart beats", HeartBeat.DELAY);
    public static final String HB_DELAY_PARAM_KEY = PARAMS_PACKAGE + "." + HB_DELAY.getName();

    private static final Logger LOG = LoggerFactory.getLogger(ModuleStateNotificationServiceImpl.class);

    private final Map<String, ConfiguredVariableItem> config = new HashMap<>();
    private Set<MonitoredService> monitored = Collections.synchronizedSet(new LinkedHashSet());
    private Lock monitoredLock = new ReentrantLock();

    private final AtomicBoolean healthMonitorRun = new AtomicBoolean(false);
    private volatile boolean active = false;

    private int heartbeatDelay = HB_DELAY.get(Integer.class);

    @Autowired
    private HeartBeatStorage storage;

    @Autowired
    @Qualifier("moduleConfigurationService")
    private ModuleConfigurationService configurationService;

    @Autowired
    private ModuleMonitoringService actionsService;

    @Autowired
    private TimeService timer;

    @Autowired
    @Qualifier("serviceRunner")
    private ExecutorService executor;

    public ModuleStateNotificationServiceImpl() {
        // prepare the configuration
        config.put(HB_DELAY_PARAM_KEY, HB_DELAY);
    }

    /**
     * To register module
     *
     * @param module module to watch
     */
    @Override
    public void register(MonitoredService module) {
        monitoredLock.lock();
        try {
            monitored.add(module);
        } finally {
            monitoredLock.unlock();
        }
        storage.saveHeartBeat(module);
        LOG.debug("Adding module {} for watching.", module);
    }

    /**
     * To un-register module
     *
     * @param module module to watch
     */
    @Override
    public void unRegister(MonitoredService module) {
        monitoredLock.lock();
        try {
            final Set<MonitoredService> changed = Collections.synchronizedSet(new LinkedHashSet(monitored));
            changed.remove(module);
            monitored = changed;
            LOG.debug("Removing module {} for watching", module);
        } finally {
            monitoredLock.unlock();
        }
    }

    /**
     * To get states of all registered modules
     *
     * @return the list of module states
     */
    @Override
    public List<ModuleHealthItem> getSystemHealth() {
        return storage.getSystemHealth();
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

    @Override
    public boolean isActive() {
        return active && healthMonitorRun.get();
    }

    public long getHeartbeatDelay() {
        return heartbeatDelay;
    }

    public void setHeartbeatDelay(int heartbeatDelay) {
        final ConfiguredVariableItem delay = config.get(HB_DELAY_PARAM_KEY);
        delay.set(Integer.valueOf(this.heartbeatDelay = heartbeatDelay));
        config.put(HB_DELAY_PARAM_KEY, delay);
    }

    public void startService() throws UnknownHostException {
        if (isActive()) return;
        LOG.info("Starting service.");

        healthMonitorRun.getAndSet(false);
        executor.submit(() -> processingHeartBeats());
        while (!healthMonitorRun.get()) ;
        register(this);
    }

    public void stopService() {

        LOG.info("Shutting down service.");
        active = false;
        synchronized (healthMonitorRun) {
            healthMonitorRun.notify();
        }
        while (healthMonitorRun.get()) ;
        unRegister(this);
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
        try {
            setHeartbeatDelay(changed.get(HB_DELAY_PARAM_KEY).get(Integer.class));
        } catch (NullPointerException e) {
        }
        config.putAll(changed);
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
        return "serviceStateScanner";
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
        return "The service to notify about changes state of registered modules";
    }

    /**
     * The handle to restart monitored service
     */
    @Override
    public void restart() {
        stopService();
        try {
            startService();
        } catch (UnknownHostException e) {

        }
    }

    @Override
    public String toString() {
        return "-ModuleStateNotificationService-";
    }

    /**
     * To check the state of the module
     *
     * @param module
     */
    protected void checkHealth(ModuleHealth module) {
        if (!isActive()) return;
        final ModuleOutput.Device moduleLog = ModuleOutputDeviceFactory.getDevice(this, LogMessage.OUTPUT_TYPE);
        moduleLog.associate("Processing heart-beat for '" + module + "' module");
        moduleLog.actionBegin();
        // service is active for the moment, process module's heart-beat
        try {
            moduleLog.out("Saving heart-beat of module ", module.toString());
            storage.saveHeartBeat(module);
            moduleLog.out("Getting config updates for module ", module.toString());
            final Map<String, ConfiguredVariableItem> updated =
                    configurationService.getUpdatedVariables(module.getModulePK(), module.getConfiguration());
            if (!updated.isEmpty()) {
                moduleLog.out("Updating module configuration.");
                module.configurationChanged(updated);
            }
            moduleLog.actionEnd();
        } catch (Throwable t) {
            LOG.error("Can't process heartbeat for module '{}'", module, t);
            moduleLog.actionFail();
        }
    }

    /**
     * For tests purposes
     */
    void unregisterAll() {
        monitored.clear();
    }

    // private methods
    private void processingHeartBeats() {
        final ModuleOutput.Device moduleLog = ModuleOutputDeviceFactory.getDevice(this, LogMessage.OUTPUT_TYPE);
        moduleLog.associate("Processing heart-beats of all registered modules");
        LOG.debug("Service starts.");
        moduleLog.out("Service starts.");
        moduleLog.actionBegin();
        healthMonitorRun.getAndSet(active = true);
        try {
            while (healthMonitorRun.get() && isActive()) {
                if (isActive()) {
                    heartBeat(moduleLog);
                }
                synchronized (healthMonitorRun) {
                    if (isActive()) healthMonitorRun.wait(heartbeatDelay);
                }
            }
            moduleLog.actionEnd();
        } catch (InterruptedException e) {
            moduleLog.actionFail();
            LOG.warn("Loop interrupted.", e);
            e.printStackTrace();
        } catch (Throwable t) {
            LOG.error("Unhandled error.", t);
            moduleLog.actionFail();
        } finally {
            healthMonitorRun.getAndSet(active = false);
            LOG.debug("Service shutdown.");
        }
    }

    private void heartBeat(final ModuleOutput.Device moduleLog) {
        monitoredLock.lock();
        try{
        monitored.forEach(module -> {moduleLog.out("Processing ", module.toString()); checkHealth(module);});
        }finally {
            monitoredLock.unlock();
        }
    }

}
