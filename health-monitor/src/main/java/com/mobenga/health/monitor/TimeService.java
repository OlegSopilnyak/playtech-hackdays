package com.mobenga.health.monitor;

import java.util.Calendar;
import java.util.Date;

/**
 * The time-service
 */
public interface TimeService {
    /**
     * To get current date-time
     *
     * @return current
     */
    Date now();

    /**
     * To get calendar
     *
     * @return current calendar
     */
    Calendar correctTime();
}
