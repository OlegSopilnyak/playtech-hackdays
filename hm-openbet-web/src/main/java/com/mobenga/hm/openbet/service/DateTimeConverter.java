package com.mobenga.hm.openbet.service;

import java.util.Calendar;
import java.util.Date;

/**
 * Service to convert date-time from and to string
 */
public interface DateTimeConverter {
    /**
     * From string to java-date
     *
     * @param jsDateTime string representation of date-time
     * @return value
     */
    Date asDate(String jsDateTime);

    /**
     * From string to java-calendar
     *
     * @param jsDateTime string representation of date-time
     * @return value
     */
    Calendar asCalendar(String jsDateTime);

    /**
     * From java-date to string
     *
     * @param dateTime value to convert
     * @return string
     */
    String asString(Date dateTime);

    /**
     * From java-calendar to string
     *
     * @param calendar value to convert
     * @return string
     */
    String asString(Calendar calendar);
}
