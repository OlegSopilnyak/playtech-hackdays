package com.mobenga.health.model.persistence;

import com.mobenga.health.model.business.ConfiguredVariableItem;
import com.mobenga.health.model.business.HeartBeat;
import com.mobenga.health.model.business.ModuleKey;
import com.mobenga.health.model.business.MonitoredAction;

/**
 * The names of storage elements
 */
public interface Storage {
    String INSTANCE_NAME = "registry";
    String MODULE_STATE_STORAGE_NAME = "module-state";
    String CONFIGURATION_STORAGE_NAME = ConfiguredVariableItem.STORAGE_NAME;
    String HEARTBEAT_STORAGE_NAME = HeartBeat.STORAGE_NAME;
    String MONITORED_ACTION_STORAGE_NAME = MonitoredAction.STORAGE_NAME;
    String HEALTH_MODULE_PK_STORAGE = ModuleKey.STORAGE_NAME;
}
