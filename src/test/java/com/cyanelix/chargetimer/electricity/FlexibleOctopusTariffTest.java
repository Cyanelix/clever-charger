package com.cyanelix.chargetimer.electricity;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class FlexibleOctopusTariffTest {
    private final Tariff tariff = new FlexibleOctopusTariff();

    @Test
    void fromDateIsAfterToDate_throwException() {
        // Given...
        ZonedDateTime fromDateTime = ZonedDateTime.parse("2020-01-02T12:00:00Z");
        ZonedDateTime toDateTime = ZonedDateTime.parse("2020-01-01T12:00:00Z");

        // When...
        Throwable thrown = catchThrowable(() ->
                tariff.getRatePeriodsBetween(fromDateTime, toDateTime));

        // Then...
        assertThat(thrown).isInstanceOf(TariffException.class)
                .hasMessage("From date must be before to date");
    }

    @Test
    void periodInsideOneRate_returnSingleRatePeriod() {
        // Given...
        ZonedDateTime fromDateTime = ZonedDateTime.parse("2020-09-17T12:00Z");
        ZonedDateTime toDateTime = ZonedDateTime.parse("2020-09-17T13:00Z");

        // When...
        List<RatePeriod> ratePeriods = tariff.getRatePeriodsBetween(fromDateTime, toDateTime);

        // Then...
        assertThat(ratePeriods).hasSize(1);

        RatePeriod ratePeriod = ratePeriods.get(0);
        assertThat(ratePeriod.getStart()).isEqualTo("2020-09-17T05:30:00Z");
        assertThat(ratePeriod.getEnd()).isEqualTo("2020-09-18T00:30:00Z");
        assertThat(ratePeriod.getPence()).isEqualTo(18.04f);
    }

    @Test
    void periodCoversTwoRatesCompletely_returnTwoRatePeriods() {
        // Given...
        ZonedDateTime fromDateTime = ZonedDateTime.parse("2020-09-17T00:30Z");
        ZonedDateTime toDateTime = ZonedDateTime.parse("2020-09-18T00:30Z");

        // When...
        List<RatePeriod> ratePeriods = tariff.getRatePeriodsBetween(fromDateTime, toDateTime);

        // Then...
        assertThat(ratePeriods).hasSize(2);

        RatePeriod ratePeriod = ratePeriods.get(0);
        assertThat(ratePeriod.getStart()).isEqualTo("2020-09-17T00:30:00Z");
        assertThat(ratePeriod.getEnd()).isEqualTo("2020-09-17T05:30:00Z");
        assertThat(ratePeriod.getPence()).isEqualTo(9.38f);

        ratePeriod = ratePeriods.get(1);
        assertThat(ratePeriod.getStart()).isEqualTo("2020-09-17T05:30:00Z");
        assertThat(ratePeriod.getEnd()).isEqualTo("2020-09-18T00:30:00Z");
        assertThat(ratePeriod.getPence()).isEqualTo(18.04f);
    }

    @Test
    void periodCoversFourTimes_returnFour() {
        // Given...
        ZonedDateTime fromDateTime = ZonedDateTime.parse("2020-09-17T00:29Z");
        ZonedDateTime toDateTime = ZonedDateTime.parse("2020-09-18T00:31Z");

        // When...
        List<RatePeriod> ratePeriods = tariff.getRatePeriodsBetween(fromDateTime, toDateTime);

        // Then...
        assertThat(ratePeriods).hasSize(4);

        RatePeriod ratePeriod = ratePeriods.get(0);
        assertThat(ratePeriod.getStart()).isEqualTo("2020-09-16T05:30:00Z");
        assertThat(ratePeriod.getEnd()).isEqualTo("2020-09-17T00:30:00Z");
        assertThat(ratePeriod.getPence()).isEqualTo(18.04f);

        ratePeriod = ratePeriods.get(1);
        assertThat(ratePeriod.getStart()).isEqualTo("2020-09-17T00:30:00Z");
        assertThat(ratePeriod.getEnd()).isEqualTo("2020-09-17T05:30:00Z");
        assertThat(ratePeriod.getPence()).isEqualTo(9.38f);

        ratePeriod = ratePeriods.get(2);
        assertThat(ratePeriod.getStart()).isEqualTo("2020-09-17T05:30:00Z");
        assertThat(ratePeriod.getEnd()).isEqualTo("2020-09-18T00:30:00Z");
        assertThat(ratePeriod.getPence()).isEqualTo(18.04f);

        ratePeriod = ratePeriods.get(3);
        assertThat(ratePeriod.getStart()).isEqualTo("2020-09-18T00:30:00Z");
        assertThat(ratePeriod.getEnd()).isEqualTo("2020-09-18T05:30:00Z");
        assertThat(ratePeriod.getPence()).isEqualTo(9.38f);
    }
}
