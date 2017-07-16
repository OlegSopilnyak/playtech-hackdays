package com.mobenga.health.model.business.out;

import com.mobenga.health.model.business.ModuleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The farm of modules output factories which create devices to output information from module
 */
public class ModuleOutputDeviceFarm {
    // the farm of factories by output types
    private  static final Map<String, ModuleOutputDevice.Factory> farm = new ConcurrentHashMap<>();
    // standard slf4j logger
    private static final Logger LOG = LoggerFactory.getLogger(ModuleOutputDeviceFarm.class);

    /**
     * To get the device for appropriate module and particular type
     *
     * @param module the owner of device
     * @param type requested type of output
     * @return the instance or null if not supported
     */
    public static ModuleOutputDevice getDevice(ModuleKey module, String type){
        LOG.debug("Creating output device type '{}'  for module '{}'", type, module);
        final ModuleOutputDevice.Factory factory = farm.get(type);
        return factory == null ? null : factory.create(module);
    }

    /**
     * To register module output devices factory
     *
     * @param factory factory to register
     */
    public static void registerDeviceFactory(ModuleOutputDevice.Factory factory){
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
    public static boolean isModuleIgnored(ModuleKey module, String type){
        final ModuleOutputDevice.Factory factory = farm.get(type);
        return factory == null ? true : factory.isModuleIgnored(module);
    }
}
