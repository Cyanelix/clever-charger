package com.cyanelix.chargetimer.tesla.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChargeStateResponse {
    @JsonProperty("response")
    private ChargeState chargeState;

    public ChargeState getChargeState() {
        return chargeState;
    }

    public void setChargeState(ChargeState chargeState) {
        this.chargeState = chargeState;
    }
}
