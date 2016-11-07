package com.mobenga.hm.openbet.service.impl;

import com.mobenga.hm.openbet.service.DateTimeConverter;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit-test for DateTimeConverterImp
 */
public class DateTimeConverterImplTest {
    private DateTimeConverter instance = new DateTimeConverterImpl();
    private final String strDateTime = "2016-11-04T12:18:09.074+02:00";
    @Test
    public void asDate() throws Exception {
        Date time = instance.asDate(strDateTime);
        assertNotNull(time);
        assertEquals(116, time.getYear());
        assertEquals(10, time.getMonth());
        assertEquals(4, time.getDate());
        assertEquals(12, time.getHours());
        assertEquals(18, time.getMinutes());
        assertEquals(9, time.getSeconds());
        assertEquals(-120, time.getTimezoneOffset());
    }

    @Test
    public void asCalendar() throws Exception {
        Calendar time = instance.asCalendar(strDateTime);
        assertNotNull(time);
        assertEquals(2016, time.get(Calendar.YEAR));
        assertEquals(10, time.get(Calendar.MONTH));
        assertEquals(4, time.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, time.get(Calendar.HOUR_OF_DAY));
        assertEquals(18, time.get(Calendar.MINUTE));
        assertEquals(9, time.get(Calendar.SECOND));
        assertEquals(7200000, time.get(Calendar.ZONE_OFFSET));
    }

    @Test
    public void asString() throws Exception {
        Date time = new Date();
        time.setYear(116);
        time.setMonth(10);
        time.setDate(4);
        time.setHours(12);
        time.setMinutes(18);
        time.setSeconds(9);
        String str = instance.asString(time);
        Date fromStr = instance.asDate(str);
        assertEquals(116, fromStr.getYear());
        assertEquals(10, fromStr.getMonth());
        assertEquals(4, fromStr.getDate());
        assertEquals(12, fromStr.getHours());
        assertEquals(18, fromStr.getMinutes());
        assertEquals(9, fromStr.getSeconds());
    }

    @Test
    public void asString1() throws Exception {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.YEAR, 2016);
        time.set(Calendar.MONTH, 10);
        time.set(Calendar.DAY_OF_MONTH, 4);
        time.set(Calendar.HOUR_OF_DAY, 12);
        time.set(Calendar.MINUTE, 18);
        time.set(Calendar.SECOND, 9);
        String str = instance.asString(time);
        Calendar fromStr = instance.asCalendar(str);
        assertEquals(2016, fromStr.get(Calendar.YEAR));
        assertEquals(10, fromStr.get(Calendar.MONTH));
        assertEquals(4, fromStr.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, fromStr.get(Calendar.HOUR_OF_DAY));
        assertEquals(18, fromStr.get(Calendar.MINUTE));
        assertEquals(9, fromStr.get(Calendar.SECOND));
    }

}