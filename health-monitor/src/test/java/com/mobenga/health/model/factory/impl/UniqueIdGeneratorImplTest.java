package com.mobenga.health.model.factory.impl;

import com.mobenga.health.monitor.UniqueIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;

/**
 * Unit-test for ID- generator
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:com/mobenga/health/monitor/factory/impl/test-basic-monitor-services.xml"})
public class UniqueIdGeneratorImplTest {

    @Autowired
    private UniqueIdGenerator instance;

    @Test
    public void testGenerate() throws Exception {
        String id = instance.generate();
        assertFalse(id.equals(instance.generate()));
    }
}