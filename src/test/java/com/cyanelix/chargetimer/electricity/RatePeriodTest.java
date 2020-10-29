package com.cyanelix.chargetimer.electricity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RatePeriodTest {
    private final ZonedDateTime dummyTime = ZonedDateTime.parse("2020-01-01T12:00Z");

    @Test
    void identicalStartAndEndTime_getSeconds_returns0() {
        PricedRatePeriod ratePeriod = new PricedRatePeriod(1f, dummyTime, dummyTime);
        assertThat(ratePeriod.getSeconds()).isEqualTo(0);
    }

    @Test
    void startTime1SecondBeforeEndTime_getSeconds_returns1() {
        PricedRatePeriod ratePeriod = new PricedRatePeriod(1f, dummyTime, dummyTime.plusSeconds(1L));
        assertThat(ratePeriod.getSeconds()).isEqualTo(1);
    }

    @Test
    void startTime1DayAfterEndTime_getSeconds_returnsNegative1Day() {
        PricedRatePeriod ratePeriod = new PricedRatePeriod(1f, dummyTime, dummyTime.minusDays(1L));
        assertThat(ratePeriod.getSeconds()).isEqualTo(-60 * 60 * 24);
    }

    @ParameterizedTest
    @CsvSource({
            "2020-01-01T10:00Z, 2020-01-01T11:00Z, false",
            "2020-01-01T11:00Z, 2020-01-01T13:00Z, true",
            "2020-01-01T11:59Z, 2020-01-01T12:00Z, false",
            "2020-01-01T11:59Z, 2020-01-01T12:01Z, true",
            "2020-01-01T12:00Z, 2020-01-01T12:01Z, false",
            "2020-01-01T13:00Z, 2020-01-01T14:00Z, false"})
    void chargeNowReturnsWhetherNowWithinStartAndEndTimes(
            String startTime, String endTime, boolean shouldChargeNow) {
        // Given...
        Clock clock = Clock.fixed(Instant.parse("2020-01-01T12:00:00Z"), ZoneId.of("UTC"));

        // When...
        RatePeriod ratePeriod = new PricedRatePeriod(1f,
                ZonedDateTime.parse(startTime), ZonedDateTime.parse(endTime));

        // Then...
        assertThat(ratePeriod.chargeNow(clock)).isEqualTo(shouldChargeNow);
    }
}