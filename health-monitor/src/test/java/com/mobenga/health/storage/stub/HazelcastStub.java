package com.mobenga.health.storage.stub;

import com.hazelcast.client.proxy.ClientQueueProxy;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
