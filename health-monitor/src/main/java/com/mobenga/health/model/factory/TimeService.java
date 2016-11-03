package com.mobenga.health.model.factory;

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
     * @return
     */
    Calendar correctTime();
}
