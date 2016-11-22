package com.mobenga.health.model.factory.impl;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.ModuleOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The factory of outputs (devices to output information from module)
 */
public class ModuleOutputDeviceFactory {
    // the farm of factories by output types
    private  static final Map<String, ModuleOutput.DeviceFactory> farm = new HashMap<>();
    // standart logger
    private static final Logger LOG = LoggerFactory.getLogger(ModuleOutputDeviceFactory.class);
    /**
     * To get the device for appropriate module and particular type
     *
     * @param module the owner of device
     * @param type requested type of output
     * @return the instance or null if not supported
     */
    public static ModuleOutput.Device getDevice(HealthItemPK module, String type){
        LOG.debug("Creating output device type '{}'  for module '{}'", type, module);
        final ModuleOutput.DeviceFactory factory = farm.get(type);
        return factory == null ? null : factory.create(module);
    }

    /**
     * To register module output devices factory
     *
     * @param factory factory to register
     */
    public static void registerDeviceFactory(ModuleOutput.DeviceFactory factory){
        LOG.debug("Registering devices factory '{}'", factory);
        farm.put(factory.getType(), factory);
    }

    /**
     * To test is ignored type of module's output for saving
     *
     * @param module module to check
     * @param type type of output
     * @return true if ignored
     */
    public static boolean isModuleIgnored(HealthItemPK module, String type){
        final ModuleOutput.DeviceFactory factory = farm.get(type);
        return factory == null ? true : factory.isModuleIgnored(module);
    }
}
