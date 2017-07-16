package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.model.business.ModuleHealth;
import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.out.ModuleOutputDevice;
import com.mobenga.health.model.business.out.ModuleOutputDeviceFarm;
import com.mobenga.health.model.business.out.log.ModuleLoggerMessage;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.storage.HeartBeatStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Realization of module state notifier
 *
 * @see ModuleStateNotificationService
 */
public class ModuleStateNotificationServiceImpl extends AbstractRunningService implements ModuleStateNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleStateNotificationServiceImpl.class);

    private final Map<String, ConfiguredVariableItem> config = new HashMap<>();

    private final Set<MonitoredService> monitored = new LinkedHashSet();

    private final Lock monitoredLock = new ReentrantLock();

    private final Object healthMonitor = new Object();

    private int heartbeatDelay = HB_DELAY.get(Integer.class);

    @Autowired
    private HeartBeatStorage storage;

    @Autowired
    private ModuleConfigurationService configurationService;

//    @Autowired
//    private ModuleMonitoringService actionsService;
//
//    @Autowired
//    private TimeService timer;
//
//    @Autowired
//    @Qualifier("serviceRunner")
//    private ExecutorService executor;

    private ModuleOutputDevice mainLoopLog;

    public ModuleStateNotificationServiceImpl() {
        // prepare the configuration
        config.put(HB_DELAY_FULL_NAME, HB_DELAY);
    }

    /**
     * Return a delay between run iterations
     *
     * @return the value
     */
    @Override
    protected long scanDelayMillis() {
        return getHeartbeatDelay();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * To register module
     *
     * @param module module to watch
     */
    @Override
    public void register(MonitoredService module) {
        Objects.requireNonNull(module, "Module for register is null");
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
     * @param module module to stop watching
     */
    @Override
    public void unRegister(MonitoredService module) {
        Objects.requireNonNull(module, "Module to un-register is null");
        monitoredLock.lock();
        try {
            for (Iterator<MonitoredService> i = monitored.iterator(); i.hasNext();) {
                if (module.equals(i.next())) {
                    i.remove();
                }
            }
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
    public List<ModuleHealth> getSystemHealth() {
        return storage.getSystemHealth();
    }

    /**
     * To get the value of Module's PK
     *
     * @return value of PK (not null)
     */
//    @Override
    public ModuleKey getModulePK() {
        return this;
    }

    public long getHeartbeatDelay() {
        return heartbeatDelay;
    }

    public void setHeartbeatDelay(int heartbeatDelay) {
        config.get(HB_DELAY_FULL_NAME).set(this.heartbeatDelay = heartbeatDelay);
    }

    public void startService() {
        super.start();
    }

    @Override
    protected void beforeStart() {
        mainLoopLog = ModuleOutputDeviceFarm.getDevice(this, ModuleLoggerMessage.LOG_OUTPUT_TYPE);
        mainLoopLog.associate("Processing heart-beats of all registered modules");
        mainLoopLog.out("Service starts.");
        mainLoopLog.actionBegin();
    }

    @Override
    protected void afterStart() {
        register(this);
    }

    public void stopService() {
        super.shutdown();
    }

    @Override
    protected void beforeStop() {
        mainLoopLog.actionEnd();
    }

    @Override
    protected void afterStop() {
        unRegister(this);
    }
    
    @Override
    protected void serviceLoopException(Throwable t) {
        if (!Objects.isNull(mainLoopLog)) mainLoopLog.actionFail();
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
        LOG.debug("External configuration changes are received '{}'", changed);
        // updating heart-beat-delay parameter
        updateParameter(changed, HB_DELAY_FULL_NAME, i -> setHeartbeatDelay(i.get(Integer.class)));
        config.putAll(changed);
    }

    /**
     * The handle to restart monitored service
     */
    @Override
    public void restart() {
        LOG.info("Restarting...");
        stopService();
        startService();
    }

    @Override
    public String toString() {
        return "-ModuleStateNotificationService-";
    }

    /**
     * To check the state of the module
     *
     * @param module the module to check health and configuration changes
     */
    protected void checkHealth(MonitoredService module) {
        if (!isActive()) {
            return;
        }
        final ModuleOutputDevice moduleLog = ModuleOutputDeviceFarm.getDevice(this, ModuleLoggerMessage.LOG_OUTPUT_TYPE);
        moduleLog.associate("Processing heart-beat for '" + module + "' module");
        moduleLog.actionBegin();
        // service is active for the moment, process module's heart-beat
        try {
            moduleLog.out("Saving heart-beat of module ", module.toString());
            storage.saveHeartBeat(module);
            moduleLog.out("Getting config updates for module ", module.toString());
            final Map<String, ConfiguredVariableItem> updated
                    = configurationService.getUpdatedVariables(module, module.getConfiguration());
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

    @Override
    protected void serviceLoopIteration() throws InterruptedException {
        if (isActive()) {
            heartBeat(mainLoopLog);
        }
    }


    /**
     * For tests purposes
     */
    void unregisterAll() {
        monitored.clear();
    }

    // private methods
    private void heartBeat(final ModuleOutputDevice moduleLog) {
        monitoredLock.lock();
        try {
            monitored.stream()
                    .peek((module)->moduleLog.out("Processing ", module.toString()))
                    .forEach(module -> checkHealth(module));
        } finally {
            monitoredLock.unlock();
        }
    }

}
