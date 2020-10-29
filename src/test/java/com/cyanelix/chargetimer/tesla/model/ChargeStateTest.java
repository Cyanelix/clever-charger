package com.cyanelix.chargetimer.tesla.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ChargeStateTest {
    @Test
    void chargingStopped_isUnplugged_false() {
        ChargeState chargeState = new ChargeState();
        chargeState.setChargingState("Stopped");

        assertThat(chargeState.isUnplugged()).isFalse();
    }

    @Test
    void chargingDisconnected_isUnplugged_true() {
        ChargeState chargeState = new ChargeState();
        chargeState.setChargingState("Disconnected");

        assertThat(chargeState.isUnplugged()).isTrue();
    }
}