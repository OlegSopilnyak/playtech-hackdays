package com.mobenga.health.model;

import com.mobenga.health.model.transport.ConfiguredVariableItemDto;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit-test
 */
public class ConfiguredVariableItemTest {

    @Test
    public void testStringGet() throws Exception {
        String expected = "str";
        ConfiguredVariableItem item = new ConfiguredVariableItemDto("test", "testing variable", expected);
        String value = item.get(String.class);
        assertEquals(expected, value);
    }

    @Test
    public void testIntegerGet() throws Exception {
        Integer expected = new Integer(1);
        ConfiguredVariableItem item = new ConfiguredVariableItemDto("test", "testing variable", expected);
        Number value = item.get(Integer.class);
        assertEquals(expected, value);
    }

    @Test
    public void testDoubleGet() throws Exception {
        Double expected = new Double(1);
        ConfiguredVariableItem item = new ConfiguredVariableItemDto("test", "testing variable", expected);
        Number value = item.get(Double.class);
        assertEquals(expected, value);
    }

    @Test
    public void testTimeStampGet() throws Exception {
        Date expected = new Date();
        ConfiguredVariableItem item = new ConfiguredVariableItemDto("test", "testing variable", expected);
        Date value = item.get(Date.class);
        assertEquals(expected, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeStampGetBad() throws Exception {
        Date expected = new Date();
        ConfiguredVariableItem item = new ConfiguredVariableItemDto("test", "testing variable", expected);
        item.get(Double.class);
        fail("Here must be thrown IllegalArgumentException");
    }

    @Test
    public void testIntegerSet() throws Exception {
        ConfiguredVariableItem item = new ConfiguredVariableItemDto("test", "testing variable", 5);
        assertEquals(Integer.valueOf(5), item.get(Integer.class));
        item.set(100);
        assertEquals(Integer.valueOf(100), item.get(Integer.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntegerSetBad() throws Exception {
        ConfiguredVariableItem item = new ConfiguredVariableItemDto("test", "testing variable", 5);
        item.set("100");
        fail("Here must be thrown IllegalArgumentException");
    }

    @Test
    public void testDoubleSet() throws Exception {
        ConfiguredVariableItem item = new ConfiguredVariableItemDto("test", "testing variable", 5.0);
        assertEquals(Double.valueOf(5), item.get(Double.class));
        item.set(100);
        assertEquals(Double.valueOf(100), item.get(Double.class));
    }

    @Test
    public void testStringSet() throws Exception {
        ConfiguredVariableItem item = new ConfiguredVariableItemDto("test", "testing variable", "5.0");
        assertEquals("5.0", item.get(String.class));
        item.set("100");
        assertEquals("100", item.get(String.class));
    }

    @Test
    public void testTimeStampSet() throws Exception {
        Date now = new Date();
        ConfiguredVariableItem item = new ConfiguredVariableItemDto("test", "testing variable", now);
        assertEquals(now, item.get(Date.class));
        Date next = new Date(now.getTime() + 10000);
        item.set(next);
        assertEquals(new Date(now.getTime() + 10000), item.get(Date.class));
        assertFalse(now.equals(next));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeStampSetBad() throws Exception {
        Date now = new Date();
        ConfiguredVariableItem item = new ConfiguredVariableItemDto("test", "testing variable", now);
        item.set(5);
        fail("Here must be thrown IllegalArgumentException");
    }
}