package com.mobenga.hm.openbet.service.impl;

import com.mobenga.hm.openbet.service.DateTimeConverter;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;
import java.util.Date;

/**
 * Date-Time converter realization
 * @see DateTimeConverter
 */
public class DateTimeConverterImpl implements DateTimeConverter{
    /**
     * From string to java-date
     *
     * @param jsDateTime string representation of date-time
     * @return value
     */
    @Override
    public Date asDate(String jsDateTime) {
        return asCalendar(jsDateTime).getTime();
    }

    /**
     * From string to java-calendar
     *
     * @param jsDateTime string representation of date-time
     * @return value
     */
    @Override
    public Calendar asCalendar(String jsDateTime) {
        return DatatypeConverter.parseDateTime(jsDateTime);
    }

    /**
     * From java-date to string
     *
     * @param dateTime value to convert
     * @return string
     */
    @Override
    public String asString(Date dateTime) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        return asString(calendar);
    }

    /**
     * From java-calendar to string
     *
     * @param calendar value to convert
     * @return string
     */
    @Override
    public String asString(Calendar calendar) {
        return DatatypeConverter.printDateTime(calendar);
    }
}
