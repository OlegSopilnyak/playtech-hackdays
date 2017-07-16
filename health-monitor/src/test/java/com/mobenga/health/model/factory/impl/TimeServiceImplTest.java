package com.mobenga.health.model.factory.impl;

import com.mobenga.health.monitor.TimeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.assertFalse;

/**
 * Test for timer-server
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:com/mobenga/health/monitor/factory/impl/test-basic-monitor-services.xml"})
public class TimeServiceImplTest {

    @Autowired
    private TimeService service;

    @Test
    public void testNow() throws Exception {
        Date now1 = service.now();
        Thread.sleep(20);
        assertFalse(now1.equals(service.now()));
    }
}