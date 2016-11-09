package com.mobenga.health.monitor.impl;

import com.mobenga.health.monitor.DistributedContainersService;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Realization of distributed containers service trivial for tests only;
 */
public class DistributedContainersServiceTrivialImpl implements DistributedContainersService {
    /**
     * To get distributed queue by name
     *
     * @param name the name of queue
     * @return instance of distributed queue
     */
    @Override
    public BlockingQueue queue(String name) {
        return new LinkedBlockingQueue<>();
    }

    /**
     * To get distributed map by name
     *
     * @param name the name of map
     * @return the instance of distributed map
     */
    @Override
    public Map map(String name) {
        return new ConcurrentHashMap<>();
    }
}
