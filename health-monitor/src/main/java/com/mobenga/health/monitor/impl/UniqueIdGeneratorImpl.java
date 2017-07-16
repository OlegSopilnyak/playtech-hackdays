package com.mobenga.health.monitor.impl;

import com.mobenga.health.monitor.UniqueIdGenerator;

import java.util.UUID;

/**
 * Service to generated unique ids
 * @see UniqueIdGenerator
 */
public class UniqueIdGeneratorImpl implements UniqueIdGenerator {
    /**
     * To generate unique id
     *
     * @return unique id
     */
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
