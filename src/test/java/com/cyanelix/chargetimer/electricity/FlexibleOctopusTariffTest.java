package com.cyanelix.chargetimer.electricity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class FlexibleOctopusTariffTest {
    private final Tariff tariff = new FlexibleOctopusTariff();

    @Test
    void fromDateIsAfterToDate_throwException() {
        // Given...
        LocalDateTime fromDateTime = LocalDateTime.MAX;
        LocalDateTime toDateTime = LocalDateTime.MIN;

        // When...
        Throwable thrown = catchThrowable(() ->
                tariff.getRatePeriodsUntil(fromDateTime, toDateTime));

        // Then...
        assertThat(thrown).isInstanceOf(TariffException.class)
                .hasMessage("From date must be before to date");
    }

    @Test
    void periodCoversOneRate_returnSingleRatePeriod() {
        // Given...
        LocalDateTime fromDateTime = LocalDateTime.of(2020, 9, 17, 12, 0);
        LocalDateTime toDateTime = LocalDateTime.of(2020, 9, 17, 13, 0);

        // When...
        List<RatePeriod> ratePeriods = tariff.getRatePeriodsUntil(fromDateTime, toDateTime);

        // Then...
        assertThat(ratePeriods).hasSize(1);

        RatePeriod ratePeriod = ratePeriods.get(0);
        assertThat(ratePeriod.getStart()).isEqualTo("2020-09-17T05:30:00Z");
        assertThat(ratePeriod.getEnd()).isEqualTo("2020-09-18T01:30:00Z");
        assertThat(ratePeriod.getPence()).isEqualTo(18.04f);
    }

    @Test
    void periodCoversTwoRatesCompletely_returnTwoRatePeriods() {

    }
}
