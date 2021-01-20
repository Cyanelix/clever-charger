package com.cyanelix.chargetimer.electricity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TimeCalculatorTest {
    private final TimeCalculator timeCalculator = new TimeCalculator();

    @Test
    void zeroPercent_takesZeroTime() {
        float seconds = timeCalculator.secondsToChargePercent(0);
        assertThat(seconds).isZero();
    }

    @Test
    void oneHundredPercent_takesTime() {
        float seconds = timeCalculator.secondsToChargePercent(100);
        assertThat(seconds).isCloseTo(40000f, within(0.05f));
    }
}