package com.mobenga.health.monitor.impl;

import com.hazelcast.core.HazelcastInstance;
import com.mobenga.health.model.ConfiguredVariableItem;
import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.HeartBeat;
import com.mobenga.health.model.transport.LocalConfiguredVariableItem;
import com.mobenga.health.model.transport.ModuleHealthItem;
import com.mobenga.health.monitor.ModuleConfigurationService;
import com.mobenga.health.monitor.behavior.ModuleHealth;
import com.mobenga.health.monitor.ModuleStateNotificationService;
import com.mobenga.health.monitor.MonitoredService;
import com.mobenga.health.storage.HeartBeatStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final Set<MonitoredService> monitored = Collections.synchronizedSet(new LinkedHashSet());
    private final AtomicBoolean healthMonitorRun = new AtomicBoolean(false);

    private volatile boolean active = false;
    private int heartbeatDelay = HB_DELAY.get(Integer.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private HeartBeatStorage storage;

    @Autowired
    @Qualifier("moduleConfigurationService")
    private ModuleConfigurationService configurationService;

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
        monitored.add(module);
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
        monitored.remove(module);
        LOG.debug("Removing module {} for watching", module);
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
        // service is active for the moment, process module's heart-beat
        try {
            storage.saveHeartBeat(module);
            final Map<String, ConfiguredVariableItem> updated =
                    configurationService.getUpdatedVariables(module.getModulePK(), module.getConfiguration());
            if (!updated.isEmpty()) module.configurationChanged(updated);
        } catch (Throwable t) {
            LOG.error("Can't process heartbeat for module '{}'", module, t);
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
        healthMonitorRun.getAndSet(active = true);
        LOG.debug("Service starts.");
        try {
            while (healthMonitorRun.get() && isActive()) {
                if (isActive()) heartBeat();
                synchronized (healthMonitorRun) {
                    if (isActive()) healthMonitorRun.wait(heartbeatDelay);
                }
            }
        } catch (InterruptedException e) {
            LOG.warn("Loop interrupted.", e);
            e.printStackTrace();
        } catch (Throwable t) {
            LOG.error("Unhandled error.", t);
        } finally {
            healthMonitorRun.getAndSet(active = false);
            LOG.debug("Service shutdown.");
        }
    }

    private void heartBeat() {
        synchronized (monitored) {
            monitored.forEach(module -> checkHealth(module));
        }
    }

}
