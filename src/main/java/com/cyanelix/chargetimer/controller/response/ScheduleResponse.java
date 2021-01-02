package com.cyanelix.chargetimer.controller.response;

import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.WeeklyTime;

import java.time.ZonedDateTime;
import java.util.Map;

public class ScheduleResponse {
    private final Map<WeeklyTime, ChargeLevel> weekly;
    private final Map<ZonedDateTime, ChargeLevel> exceptions;

    public ScheduleResponse(Map<WeeklyTime, ChargeLevel> weekly, Map<ZonedDateTime, ChargeLevel> exceptions) {
        this.weekly = weekly;
        this.exceptions = exceptions;
    }

    public Map<WeeklyTime, ChargeLevel> getWeekly() {
        return weekly;
    }

    public Map<ZonedDateTime, ChargeLevel> getExceptions() {
        return exceptions;
    }
}
