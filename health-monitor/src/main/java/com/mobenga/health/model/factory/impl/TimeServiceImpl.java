package com.mobenga.health.model.factory.impl;

import com.mobenga.health.model.factory.TimeService;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Implementation of TimeService
 * @see com.mobenga.health.model.factory.TimeService
 */
public class TimeServiceImpl implements TimeService{
    private TimeZone zone  = TimeZone.getDefault();
    private Locale locale = Locale.getDefault(Locale.Category.FORMAT);
    /**
     * To get current date-time
     *
     * @return current
     */
    @Override
    public Date now() {
        return correctTime().getTime();
    }

    /**
     * To get calendar
     *
     * @return current instance for appropriate time-zone and locale
     */
    @Override
    public Calendar correctTime() {
        return Calendar.getInstance(zone, locale);
    }

    public TimeZone getZone() {
        return zone;
    }

    public void setZone(TimeZone zone) {
        this.zone = zone;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        return "TimeServiceImpl{" +
                "zone=" + zone +
                ", locale=" + locale +
                '}';
    }
}
