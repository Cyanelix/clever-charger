package com.cyanelix.chargetimer.charges;

import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import com.cyanelix.chargetimer.microtypes.WeeklyTime;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class RequiredChargesRepositoryTest {
    private final Clock clock = Clock.fixed(Instant.parse("2020-01-01T12:00:00Z"), ZoneId.of("UTC"));

    private final RequiredChargesRepository repository = new RequiredChargesRepository(clock);

    @Test
    void noCharges_returnNull() {
        // When...
        RequiredCharge next = repository.getNextRequiredCharge();

        // Then...
        assertThat(next).isNull();
    }

    @Test
    void addException_getNextRequiredCharge_returnException() {
        // Given...
        RequiredCharge requiredCharge = RequiredCharge.of(ChargeLevel.of(50),
                ZonedDateTime.parse("2020-01-01T12:00Z"));
        repository.addException(requiredCharge);

        // When...
        RequiredCharge next = repository.getNextRequiredCharge();

        // Then..
        assertThat(next).isEqualTo(requiredCharge);
    }

    @Test
    void noExceptions_oneWeeklyThisWeek_getNextRequiredCharge_returnWeekly() {
        // Given...
        repository.addWeekly(new WeeklyTime(DayOfWeek.FRIDAY, LocalTime.NOON), ChargeLevel.of(50));

        // When...
        RequiredCharge next = repository.getNextRequiredCharge();

        // Then...
        assertThat(next.getRequiredBy()).isEqualTo("2020-01-03T12:00:00Z");
        assertThat(next.getChargeLevel().getValue()).isEqualTo(50);
    }

    @Test
    void noExceptions_oneWeeklyLaterToday_getNextRequiredCharge_returnWeekly() {
        // Given...
        repository.addWeekly(new WeeklyTime(DayOfWeek.WEDNESDAY, LocalTime.of(12, 1)),
                ChargeLevel.of(50));

        // When...
        RequiredCharge next = repository.getNextRequiredCharge();

        // Then...
        assertThat(next.getRequiredBy()).isEqualTo("2020-01-01T12:01:00Z");
        assertThat(next.getChargeLevel().getValue()).isEqualTo(50);
    }

    @Test
    void noExceptions_oneWeeklyNextWeek_getNextRequiredCharge_returnWeekly() {
        // Given...
        repository.addWeekly(new WeeklyTime(DayOfWeek.MONDAY, LocalTime.NOON), ChargeLevel.of(50));

        // When...
        RequiredCharge next = repository.getNextRequiredCharge();

        // Then...
        assertThat(next.getRequiredBy()).isEqualTo("2020-01-06T12:00:00Z");
        assertThat(next.getChargeLevel().getValue()).isEqualTo(50);
    }

    @Test
    void oneExceptionBeforeOneWeekly_getNextRequiredCharged_returnException() {
        // Given...
        RequiredCharge requiredCharge = RequiredCharge.of(ChargeLevel.of(50),
                ZonedDateTime.parse("2020-01-04T12:00Z"));
        repository.addException(requiredCharge);

        repository.addWeekly(new WeeklyTime(DayOfWeek.MONDAY, LocalTime.NOON), ChargeLevel.of(60));

        // When...
        RequiredCharge next = repository.getNextRequiredCharge();

        // Then...
        assertThat(next.getRequiredBy()).isEqualTo("2020-01-04T12:00:00Z");
        assertThat(next.getChargeLevel().getValue()).isEqualTo(50);
    }

    @Test
    void oneExceptionAfterOneWeekly_getNextRequiredCharged_returnWeekly() {
        // Given...
        RequiredCharge requiredCharge = RequiredCharge.of(ChargeLevel.of(50),
                ZonedDateTime.parse("2020-01-07T12:00Z"));
        repository.addException(requiredCharge);

        repository.addWeekly(new WeeklyTime(DayOfWeek.MONDAY, LocalTime.NOON), ChargeLevel.of(60));

        // When...
        RequiredCharge next = repository.getNextRequiredCharge();

        // Then...
        assertThat(next.getRequiredBy()).isEqualTo("2020-01-06T12:00:00Z");
        assertThat(next.getChargeLevel().getValue()).isEqualTo(60);
    }
}