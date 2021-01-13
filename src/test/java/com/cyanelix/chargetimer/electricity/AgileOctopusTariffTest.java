package com.cyanelix.chargetimer.electricity;

import com.cyanelix.chargetimer.octopus.AveragedAgileRates;
import com.cyanelix.chargetimer.octopus.FutureRates;
import com.cyanelix.chargetimer.octopus.model.UnitRate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AgileOctopusTariffTest {
    @Mock
    private FutureRates futureRates;

    @Mock
    private AveragedAgileRates averagedAgileRates;

    @InjectMocks
    private AgileOctopusTariff tariff;

    @Test
    void fromDateIsAfterToDate_returnEmptyList() {
        List<PricedRatePeriod> ratePeriods = tariff.getRatePeriodsBetween(
                ZonedDateTime.parse("2021-01-31T12:00Z"),
                ZonedDateTime.parse("2021-01-01T12:00Z"));

        assertThat(ratePeriods).isEmpty();
    }

    @Test
    void requestedPeriodFullyWithinFutureKnownPeriods() {
        List<UnitRate> futureUnitRates = Arrays.asList(
                unitRate("2021-01-01T12:00Z", 1),
                unitRate("2021-01-01T12:30Z", 2));
        given(futureRates.getUnitRatesFromNow()).willReturn(futureUnitRates);

        List<PricedRatePeriod> ratePeriods = tariff.getRatePeriodsBetween(
                ZonedDateTime.parse("2021-01-01T12:00Z"),
                ZonedDateTime.parse("2021-01-01T13:00Z"));

        assertThat(ratePeriods).hasSize(2);
        assertThat(ratePeriods).extracting(PricedRatePeriod::getPence)
                .containsExactly(1.0f, 2.0f);
    }

    @Test
    void requestedPeriodFullyBeforeFutureKnownPeriods() {
        ZonedDateTime periodStart = ZonedDateTime.parse("2021-01-01T09:00Z");
        ZonedDateTime periodEnd = ZonedDateTime.parse("2021-01-01T10:00Z");

        given(futureRates.getUnitRatesFromNow()).willReturn(Arrays.asList(
                unitRate("2021-01-01T12:00Z", 1),
                unitRate("2021-01-01T12:30Z", 2)));

        given(averagedAgileRates.getAverageRatesBetween(periodStart, periodEnd))
                .willReturn(Arrays.asList(
                        unitRate("2021-01-01T09:00Z", 3),
                        unitRate("2021-01-01T09:30Z", 4)));

        List<PricedRatePeriod> ratePeriods = tariff.getRatePeriodsBetween(
                periodStart, periodEnd);

        assertThat(ratePeriods).hasSize(2);
        assertThat(ratePeriods).extracting(PricedRatePeriod::getPence)
                .containsExactly(3.0f, 4.0f);
    }

    @Test
    void requestedPeriodFullyAfterFutureKnownPeriods() {
        ZonedDateTime periodStart = ZonedDateTime.parse("2021-01-01T14:00Z");
        ZonedDateTime periodEnd = ZonedDateTime.parse("2021-01-01T15:00Z");

        given(futureRates.getUnitRatesFromNow()).willReturn(Arrays.asList(
                unitRate("2021-01-01T12:00Z", 1),
                unitRate("2021-01-01T12:30Z", 2)));

        given(averagedAgileRates.getAverageRatesBetween(periodStart, periodEnd))
                .willReturn(Arrays.asList(
                        unitRate("2021-01-01T14:00Z", 5),
                        unitRate("2021-01-01T14:30Z", 6)));

        List<PricedRatePeriod> ratePeriods = tariff.getRatePeriodsBetween(
                periodStart, periodEnd);

        assertThat(ratePeriods).hasSize(2);
        assertThat(ratePeriods).extracting(PricedRatePeriod::getPence)
                .containsExactly(5.0f, 6.0f);
    }

    @Test
    void requestedPeriodPassesEndOfFutureKnownPeriods() {
        ZonedDateTime periodStart = ZonedDateTime.parse("2021-01-01T12:00Z");
        ZonedDateTime periodEnd = ZonedDateTime.parse("2021-01-01T14:00Z");

        given(futureRates.getUnitRatesFromNow()).willReturn(Arrays.asList(
                unitRate("2021-01-01T12:00Z", 1),
                unitRate("2021-01-01T12:30Z", 2)));

        ZonedDateTime startOfAverageTime = ZonedDateTime.parse("2021-01-01T13:00Z");

        given(averagedAgileRates.getAverageRatesBetween(startOfAverageTime, periodEnd))
                .willReturn(Arrays.asList(
                        unitRate("2021-01-01T13:00Z", 3),
                        unitRate("2021-01-01T13:30Z", 4)));

        List<PricedRatePeriod> ratePeriods = tariff.getRatePeriodsBetween(
                periodStart, periodEnd);

        assertThat(ratePeriods).hasSize(4);
        assertThat(ratePeriods).extracting(PricedRatePeriod::getPence)
                .containsExactly(1.0f, 2.0f, 3.0f, 4.0f);
    }

    @Test
    void requestedPeriodSpansStartOfFutureKnownPeriods() {
        ZonedDateTime periodStart = ZonedDateTime.parse("2021-01-01T12:00Z");
        ZonedDateTime periodEnd = ZonedDateTime.parse("2021-01-01T14:00Z");

        given(futureRates.getUnitRatesFromNow()).willReturn(Arrays.asList(
                unitRate("2021-01-01T13:00Z", 1),
                unitRate("2021-01-01T13:30Z", 2)));

        ZonedDateTime endOfAverageTime = ZonedDateTime.parse("2021-01-01T13:00Z");

        given(averagedAgileRates.getAverageRatesBetween(periodStart, endOfAverageTime))
                .willReturn(Arrays.asList(
                        unitRate("2021-01-01T12:00Z", 3),
                        unitRate("2021-01-01T12:30Z", 4)));

        List<PricedRatePeriod> ratePeriods = tariff.getRatePeriodsBetween(
                periodStart, periodEnd);

        assertThat(ratePeriods).hasSize(4);
        assertThat(ratePeriods).extracting(PricedRatePeriod::getPence)
                .containsExactly(3.0f, 4.0f, 1.0f, 2.0f);
    }

    @Test
    void requestedPeriodSpansBothSidesOfFutureKnownPeriods() {
        ZonedDateTime periodStart = ZonedDateTime.parse("2021-01-01T12:00Z");
        ZonedDateTime periodEnd = ZonedDateTime.parse("2021-01-01T14:00Z");

        given(futureRates.getUnitRatesFromNow()).willReturn(Arrays.asList(
                unitRate("2021-01-01T12:30Z", 1),
                unitRate("2021-01-01T13:00Z", 2)));

        given(averagedAgileRates.getAverageRatesBetween(periodStart,
                ZonedDateTime.parse("2021-01-01T12:30Z")))
                .willReturn(Collections.singletonList(
                        unitRate("2021-01-01T12:00Z", 3)));

        given(averagedAgileRates.getAverageRatesBetween(
                ZonedDateTime.parse("2021-01-01T13:30Z"),
                periodEnd))
                .willReturn(Collections.singletonList(
                        unitRate("2021-01-01T13:30Z", 4)));

        List<PricedRatePeriod> ratePeriods = tariff.getRatePeriodsBetween(
                periodStart, periodEnd);

        assertThat(ratePeriods).hasSize(4);
        assertThat(ratePeriods).extracting(PricedRatePeriod::getPence)
                .containsExactly(3.0f, 1.0f, 2.0f, 4.0f);
    }

    private UnitRate unitRate(String startTime, int price) {
        ZonedDateTime startDateTime = ZonedDateTime.parse(startTime);
        ZonedDateTime endDateTime = startDateTime.plusMinutes(30L);
        return new UnitRate(BigDecimal.valueOf(price), startDateTime, endDateTime);
    }
}