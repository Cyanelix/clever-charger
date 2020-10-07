package com.cyanelix.chargetimer.electricity;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RatePeriodTest {
    private ZonedDateTime dummyTime = ZonedDateTime.parse("2020-01-01T12:00Z");

    @Test
    void identicalStartAndEndTime_getSeconds_returns0() {
        RatePeriod ratePeriod = new RatePeriod(1f, dummyTime, dummyTime);
        assertThat(ratePeriod.getSeconds()).isEqualTo(0);
    }

    @Test
    void startTime1SecondBeforeEndTime_getSeconds_returns1() {
        RatePeriod ratePeriod = new RatePeriod(1f, dummyTime, dummyTime.plusSeconds(1L));
        assertThat(ratePeriod.getSeconds()).isEqualTo(1);
    }

    @Test
    void startTime1DayAfterEndTime_getSeconds_returnsNegative1Day() {
        RatePeriod ratePeriod = new RatePeriod(1f, dummyTime, dummyTime.minusDays(1L));
        assertThat(ratePeriod.getSeconds()).isEqualTo(-60 * 60 * 24);
    }
}