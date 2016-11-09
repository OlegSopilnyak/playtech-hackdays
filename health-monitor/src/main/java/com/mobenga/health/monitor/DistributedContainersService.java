package com.mobenga.health.monitor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Service to provide distributed container by name (Map,Queue)
 */
public interface DistributedContainersService {
    /**
     * To get distributed queue by name
     *
     * @param name the name of queue
     * @return instance of distributed queue
     */
    BlockingQueue queue(String name);

    /**
     * To get distributed map by name
     *
     * @param name the name of map
     * @return the instance of distributed map
     */
    Map map(String name);
}
