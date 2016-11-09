package com.mobenga.health.monitor.impl;

import com.hazelcast.core.HazelcastInstance;
import com.mobenga.health.monitor.DistributedContainersService;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Realization of distributed containers service based on Hazelcast
 */
public class DistributedContainersServiceHazelcastImpl implements DistributedContainersService {
    private HazelcastInstance cacheSystem;

    /**
     * To get distributed queue by name
     *
     * @param name the name of queue
     * @return instance of distributed queue
     */
    @Override
    public BlockingQueue queue(String name) {
        return cacheSystem.getQueue(name);
    }

    /**
     * To get distributed map by name
     *
     * @param name the name of map
     * @return the instance of distributed map
     */
    @Override
    public Map map(String name) {
        return cacheSystem.getMap(name);
    }

    public HazelcastInstance getCacheSystem() {
        return cacheSystem;
    }

    public void setCacheSystem(HazelcastInstance cacheSystem) {
        this.cacheSystem = cacheSystem;
    }
}
