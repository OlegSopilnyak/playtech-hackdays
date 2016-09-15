package com.mobenga.health.model.factory.impl;

import com.mobenga.health.model.factory.TimeService;

import java.util.Date;

/**
 * Implementation of TimeService
 * @see com.mobenga.health.model.factory.TimeService
 */
public class TimeServiceImpl implements TimeService{
    @Override
    public Date now() {
        return new Date();
    }
}
