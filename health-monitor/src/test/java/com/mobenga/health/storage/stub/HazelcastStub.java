package com.mobenga.health.storage.stub;

import com.hazelcast.core.HazelcastInstance;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Mockito.mock;

/**
 * The stub for Hazelcast
 */
public class HazelcastStub {
    private static HazelcastInstance newHazelcastInstance(){
        HazelcastInstance instance = mock(HazelcastInstance.class);
        final BlockingQueue queue = new LinkedBlockingQueue();
//        when(instance.queue(anyString())).thenReturn(queue);
        return instance;
    }
}
