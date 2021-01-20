package com.cyanelix.chargetimer.electricity;

import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ChargeCalculatorTest {
    @Mock
    private Tariff mockTariff;

    private final Clock clock = Clock.fixed(Instant.parse("2020-01-01T01:00:00Z"), ZoneId.of("UTC"));

    private ChargeCalculator chargeCalculator;

    @BeforeEach
    void setUp() {
        chargeCalculator = new ChargeCalculator(new TimeCalculator(), mockTariff, clock);
    }

    @ParameterizedTest(name = "{0} by {1}, with current SOC {2}; charge at {3}")
    @MethodSource("provideChargeParameters")
    void chargeCalculatorPermutations(
            ChargeLevel requiredLevel, ZonedDateTime requiredBy, ChargeLevel currentCharge,
            ZonedDateTime expected) {
        // Given...
        if (expected != null) {
            mockTariff(requiredBy);
        }

        RequiredCharge requiredCharge = RequiredCharge.of(requiredLevel, requiredBy);

        // When...
        RatePeriod nextChargePeriod = chargeCalculator.getNextChargePeriod(
                requiredCharge, currentCharge);

        // Then...
        if (expected == null) {
            assertThat(nextChargePeriod).isSameAs(RatePeriod.NULL_RATE_PERIOD);
        } else {
            assertThat(((PricedRatePeriod) nextChargePeriod).getStart())
                    .isEqualTo(expected);
        }
    }

    private static Stream<Arguments> provideChargeParameters() {
        ZonedDateTime now = ZonedDateTime.parse("2020-01-01T01:00Z");

        return Stream.of(
                // Already above target charge.
                Arguments.of(ChargeLevel.of(50), now, ChargeLevel.of(100),
                        null),
                // Big charge needed, start immediately.
                Arguments.of(ChargeLevel.of(100), now.plusHours(5), ChargeLevel.of(0),
                        ZonedDateTime.parse("2020-01-01T01:00Z")),
                // Small charge needed soon, start immediately.
                Arguments.of(ChargeLevel.of(100), now.plusHours(1), ChargeLevel.of(90),
                        ZonedDateTime.parse("2020-01-01T01:00Z")),
                // Small charge needed later, start at cheapest time.
                Arguments.of(ChargeLevel.of(100), now.plusHours(5), ChargeLevel.of(95),
                        ZonedDateTime.parse("2020-01-01T03:00Z")),
                // Charge needed immediately, no tariffs returned.
                Arguments.of(ChargeLevel.of(100), now, ChargeLevel.of(0), null)
        );
    }

    private void mockTariff(ZonedDateTime maxDateTime) {
        List<PricedRatePeriod> mockRates = Stream.of(
                new PricedRatePeriod(3f, ZonedDateTime.parse("2020-01-01T01:00Z"),
                        ZonedDateTime.parse("2020-01-01T02:00Z")),
                new PricedRatePeriod(2f, ZonedDateTime.parse("2020-01-01T02:00Z"),
                        ZonedDateTime.parse("2020-01-01T03:00Z")),
                new PricedRatePeriod(1f, ZonedDateTime.parse("2020-01-01T03:00Z"),
                        ZonedDateTime.parse("2020-01-01T04:00Z")),
                new PricedRatePeriod(2f, ZonedDateTime.parse("2020-01-01T04:00Z"),
                        ZonedDateTime.parse("2020-01-01T05:00Z")),
                new PricedRatePeriod(3f, ZonedDateTime.parse("2020-01-01T05:00Z"),
                        ZonedDateTime.parse("2020-01-01T06:00Z")))
                .filter(ratePeriod -> ratePeriod.getStart().isBefore(maxDateTime))
                .collect(Collectors.toList());

        given(mockTariff.getRatePeriodsBetween(any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .willReturn(mockRates);
    }
}