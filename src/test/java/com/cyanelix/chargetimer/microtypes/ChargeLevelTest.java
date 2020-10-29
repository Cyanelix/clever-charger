package com.cyanelix.chargetimer.microtypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ChargeLevelTest {
    @Test
    void createChargeLevel_returnsChargeLevel() {
        // Given...
        int value = 50;

        // When...
        ChargeLevel chargeLevel = ChargeLevel.of(value);

        // Then...
        assertThat(chargeLevel.getValue()).isEqualTo(value);
    }

    @Test
    void chargeLevelAbove100_throwsException() {
        // When...
        Throwable thrown = catchThrowable(() ->
                ChargeLevel.of(101));

        // Then...
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Charge level must be no more than 100");
    }

    @Test
    void chargeLevelBelow0_throwsException() {
        // When...
        Throwable thrown = catchThrowable(() ->
                ChargeLevel.of(-1));

        // Then...
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Charge level must be at least 0");
    }

    @Test
    void chargeLevel_toString() {
        // Given...
        ChargeLevel chargeLevel = ChargeLevel.of(100);

        // When...
        String value = chargeLevel.toString();

        // Then...
        assertThat(value).isEqualTo("100%");
    }

    @ParameterizedTest()
    @CsvSource({
            "100, 50, true",
            "50, 100, false",
            "0, 0, true",
            "100, 100, true"})
    void testEqualsOrExceeds(int left, int right, boolean expectedResult) {
        assertThat(ChargeLevel.of(left).equalsOrExceeds(ChargeLevel.of(right)))
                .isEqualTo(expectedResult);
    }
}