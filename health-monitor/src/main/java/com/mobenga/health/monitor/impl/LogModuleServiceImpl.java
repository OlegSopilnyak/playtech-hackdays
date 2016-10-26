package com.mobenga.health.monitor.impl;

import com.mobenga.health.model.HealthItemPK;
import com.mobenga.health.model.LogMessage;
import com.mobenga.health.model.ModuleOutput;
import com.mobenga.health.model.factory.UniqueIdGenerator;
import com.mobenga.health.model.factory.impl.ModuleOutputDeviceFactory;
import com.mobenga.health.storage.ModuleOutputStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The service handles module's output type "log"
 */
@Service("logService")
public class LogModuleServiceImpl implements ModuleOutput.DeviceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(LogModuleServiceImpl.class);

    @Autowired
    @Qualifier("serviceRunner")
    private ExecutorService executor;
    @Autowired
    private UniqueIdGenerator idGenerator;

    private final BlockingQueue<Event> eventsQueue = new LinkedBlockingQueue<>();

    @Autowired
    private ModuleOutputStorage storage;

    private final AtomicBoolean serviceWorks = new AtomicBoolean(false);

    public void startService(){
        if (serviceWorks.get()) return;
        LOG.info("Starting service");
        executor.submit(()->processEvents());
        while (!serviceWorks.get());
        // register the device
        ModuleOutputDeviceFactory.registerDeviceFactory(this);
    }
    /**
     * To create the Device for module's output
     *
     * @param module
     * @return the instance
     */
    @Override
    public ModuleOutput.Device create(HealthItemPK module) {
        return new LogDevice(module);
    }

    /**
     * returns supported type of ModuleOutput
     *
     * @return the value
     */
    @Override
    public String getType() {
        return LogMessage.OUTPUT_TYPE;
    }

    // private methods
    private void processEvents(){
        serviceWorks.getAndSet(true);
        try{
            while (serviceWorks.get()){
                final Event event = eventsQueue.take();
                if (Event.NULL == event){
                    LOG.debug("Received signal to stop service.");
                    break;
                }
                saveEvent(event);
            }
        } catch (InterruptedException e) {
            LOG.error("Events queue iterrupted", e);
        } catch (Throwable t) {
            LOG.error("Caught unexpected exception", t);
        } finally {
            serviceWorks.getAndSet(false);
            LOG.info("Service stopped");
        }
    }

    private void saveEvent(Event event) {
        final LogMessage message = (LogMessage) storage.createModuleOutput(event.module, LogMessage.OUTPUT_TYPE);
        message.setId(idGenerator.generate());
        message.setActionId(event.actionId);
        final StringBuilder msg = new StringBuilder();
        for(Object arg : event.arguments) msg.append(arg);
        message.setPayload(msg.toString());
        storage.saveModuleOutput(message);
    }

    // inner classes
    private static class Event {
        // marker of end of working
        public static Event NULL = new Event(null,null,null);
        // event's parameters
        private final HealthItemPK module;
        private final String actionId;
        private final Object [] arguments;

        public Event(HealthItemPK module, String actionId, Object[] arguments) {
            this.module = module;
            this.actionId = actionId;
            this.arguments = arguments;
        }
    }
    private class LogDevice implements ModuleOutput.Device {
        private final HealthItemPK module;

        public LogDevice(HealthItemPK module) {
            this.module = module;
        }

        /**
         * To create ModuleOutput instance and send it to appropriate storage
         *
         * @param actionId   the id of action-aggregator (may be null)
         * @param arguments the arguments of output item for payload
         */
        @Override
        public void out(String actionId, Object... arguments) {
            LOG.debug("Output from module '{}'", module);
            eventsQueue.offer(new Event(module, actionId, arguments));
        }
    }
}
