package com.cyanelix.chargetimer.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TimeTemporalAdjusterTest {
    private static final ZoneId UTC = ZoneId.of("UTC");
    private final TemporalAdjuster temporalAdjuster = new TimeTemporalAdjuster(LocalTime.of(14, 0));

    @ParameterizedTest
    @MethodSource("provideDates")
    void timeBeforeNextOccurrence(ZonedDateTime initial, ZonedDateTime expected) {
        ZonedDateTime adjusted = initial.with(temporalAdjuster);
        assertThat(adjusted).isEqualTo(expected);
    }

    private static Stream<Arguments> provideDates() {
        return Stream.of(
                Arguments.of(ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, UTC),
                        ZonedDateTime.of(2020, 1, 1, 14, 0, 0, 0, UTC)),
                Arguments.of(ZonedDateTime.of(2020, 1, 1, 15, 0, 0, 0, UTC),
                        ZonedDateTime.of(2020, 1, 2, 14, 0, 0, 0, UTC)),
                Arguments.of(ZonedDateTime.of(2020, 1, 1, 14, 0, 0, 0, UTC),
                        ZonedDateTime.of(2020, 1, 2, 14, 0, 0, 0, UTC))
        );
    }
}