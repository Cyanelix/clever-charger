package com.cyanelix.chargetimer.tesla.model;

import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ChargeState {
    private int batteryLevel;
    private String chargingState;

    public boolean isUnplugged() {
        return chargingState.equals("Disconnected");
    }

    public boolean isCharging() {
        return chargingState.equals("Charging");
    }

    public boolean isReadyToCharge() {
        return chargingState.equals("Stopped");
    }

    public ChargeLevel getChargeLevel() {
        return ChargeLevel.of(batteryLevel);
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getChargingState() {
        return chargingState;
    }

    public void setChargingState(String chargingState) {
        this.chargingState = chargingState;
    }
}
