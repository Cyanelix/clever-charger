package com.cyanelix.chargetimer.octopus;

import com.cyanelix.chargetimer.octopus.model.UnitRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AveragedAgileRatesTest {
    private final AveragedAgileRates averagedAgileRates = new AveragedAgileRates();

    @Test
    void startTimeAfterEndTime_returnEmptyList() {
        List<UnitRate> rates = averagedAgileRates.getAverageRatesBetween(
                ZonedDateTime.parse("2021-01-01T12:00:00Z"),
                ZonedDateTime.parse("2021-01-01T11:00:00Z"));

        assertThat(rates).isEmpty();
    }

    @Test
    void exactlySelectSinglePeriod_returnSinglePeriod() {
        List<UnitRate> rates = averagedAgileRates.getAverageRatesBetween(
                ZonedDateTime.parse("2021-01-01T11:00:00Z"),
                ZonedDateTime.parse("2021-01-01T11:30:00Z"));

        assertThat(rates).containsExactly(new UnitRate(new BigDecimal("10.11419595"),
                ZonedDateTime.parse("2021-01-01T11:00:00Z"),
                ZonedDateTime.parse("2021-01-01T11:30:00Z")));
    }

    @Test
    void exactlySelectThreePeriods_returnThosePeriods() {
        List<UnitRate> rates = averagedAgileRates.getAverageRatesBetween(
                ZonedDateTime.parse("2021-01-01T11:00:00Z"),
                ZonedDateTime.parse("2021-01-01T12:30:00Z"));

        assertThat(rates).containsExactly(
                new UnitRate(new BigDecimal("10.11419595"),
                        ZonedDateTime.parse("2021-01-01T11:00:00Z"),
                        ZonedDateTime.parse("2021-01-01T11:30:00Z")),
                new UnitRate(new BigDecimal("9.748654054"),
                        ZonedDateTime.parse("2021-01-01T11:30:00Z"),
                        ZonedDateTime.parse("2021-01-01T12:00:00Z")),
                new UnitRate(new BigDecimal("9.714117568"),
                        ZonedDateTime.parse("2021-01-01T12:00:00Z"),
                        ZonedDateTime.parse("2021-01-01T12:30:00Z")));
    }

    @Test
    void selectWithinOnePeriod_returnThatPeriod() {
        List<UnitRate> rates = averagedAgileRates.getAverageRatesBetween(
                ZonedDateTime.parse("2021-01-01T11:01:00Z"),
                ZonedDateTime.parse("2021-01-01T11:03:00Z"));

        assertThat(rates).containsExactly(new UnitRate(new BigDecimal("10.11419595"),
                ZonedDateTime.parse("2021-01-01T11:00:00Z"),
                ZonedDateTime.parse("2021-01-01T11:30:00Z")));
    }

    @Test
    void selectSpanningTwoPeriods_returnThosePeriods() {
        List<UnitRate> rates = averagedAgileRates.getAverageRatesBetween(
                ZonedDateTime.parse("2021-01-01T11:17:00Z"),
                ZonedDateTime.parse("2021-01-01T11:43:00Z"));

        assertThat(rates).containsExactly(
                new UnitRate(new BigDecimal("10.11419595"),
                        ZonedDateTime.parse("2021-01-01T11:00:00Z"),
                        ZonedDateTime.parse("2021-01-01T11:30:00Z")),
                new UnitRate(new BigDecimal("9.748654054"),
                        ZonedDateTime.parse("2021-01-01T11:30:00Z"),
                        ZonedDateTime.parse("2021-01-01T12:00:00Z")));
    }
}