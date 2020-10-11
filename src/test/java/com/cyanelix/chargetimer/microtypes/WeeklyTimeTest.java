package com.cyanelix.chargetimer.microtypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyTimeTest {
    @Test
    void weeklyTimeNow() {
        // Given...
        Clock clock = Clock.fixed(Instant.parse("2020-01-01T12:01:02Z"), ZoneId.of("UTC"));

        // When...
        WeeklyTime weeklyTime = WeeklyTime.now(clock);

        // Then...
        assertThat(weeklyTime.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(weeklyTime.getLocalTime()).isEqualTo("12:01:02");
    }

    @ParameterizedTest(name = "{0} .. {1}")
    @MethodSource("provideWeeklyTimes")
    void testOrdering(WeeklyTime first, WeeklyTime second, Consumer<Integer> expected) {
        expected.accept(first.compareTo(second));
    }

    private static Stream<Arguments> provideWeeklyTimes() {
        Consumer<Integer> isNegative = comparison -> assertThat(comparison).isNegative();
        Consumer<Integer> isPositive = comparison -> assertThat(comparison).isPositive();
        Consumer<Integer> isZero = comparison -> assertThat(comparison).isZero();

        return Stream.of(
                Arguments.of(
                        new WeeklyTime(DayOfWeek.MONDAY, LocalTime.MIN),
                        new WeeklyTime(DayOfWeek.MONDAY, LocalTime.MAX),
                        isNegative),
                Arguments.of(
                        new WeeklyTime(DayOfWeek.MONDAY, LocalTime.MAX),
                        new WeeklyTime(DayOfWeek.MONDAY, LocalTime.MIN),
                        isPositive),
                Arguments.of(
                        new WeeklyTime(DayOfWeek.MONDAY, LocalTime.NOON),
                        new WeeklyTime(DayOfWeek.MONDAY, LocalTime.NOON),
                        isZero),
                Arguments.of(
                        new WeeklyTime(DayOfWeek.MONDAY, LocalTime.NOON),
                        new WeeklyTime(DayOfWeek.TUESDAY, LocalTime.NOON),
                        isNegative),
                Arguments.of(
                        new WeeklyTime(DayOfWeek.SUNDAY, LocalTime.NOON),
                        new WeeklyTime(DayOfWeek.MONDAY, LocalTime.NOON),
                        isPositive)
        );
    }
}