package com.mobenga.health.configuration;

import com.mobenga.health.storage.impl.SimpleFileStorageImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.*;

/**
 * The test of persistence configuration
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
        @ContextConfiguration(classes = PersistenceConfiguration.class, loader = AnnotationConfigContextLoader.class)
})
public class PersistenceConfigurationTest {
    @Autowired
    private SimpleFileStorageImpl storage;

    @Test
    public void testTheStorage() throws Exception {
        assertNotNull(storage);
    }

}