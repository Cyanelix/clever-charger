package com.cyanelix.chargetimer.controller.request;

import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.WeeklyTime;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.DayOfWeek;
import java.time.LocalTime;

public final class WeeklyRequirement {
    private final DayOfWeek day;
    private final LocalTime time;
    private final ChargeLevel chargeLevel;

    @JsonCreator
    public WeeklyRequirement(
            @JsonProperty String day,
            @JsonProperty String time,
            @JsonProperty int chargeLevel) {
        this.day = DayOfWeek.valueOf(day);
        this.time = LocalTime.parse(time);
        this.chargeLevel = ChargeLevel.of(chargeLevel);
    }

    public WeeklyTime getWeeklyTime() {
        return new WeeklyTime(day, time);
    }

    public ChargeLevel getChargeLevel() {
        return chargeLevel;
    }
}
