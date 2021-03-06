package com.cyanelix.chargetimer.tesla.model;

import com.cyanelix.chargetimer.microtypes.ChargeLevel;
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

    /**
     * This means we've used the last-known charge state, because an up-to-date value
     * was not available, probably because the car was asleep or out of network range.
     * @return True if unknown state, false otherwise.
     */
    public boolean isUnknown() {
        return chargingState.equals("Unknown");
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

    public boolean isFullyCharged() {
        return batteryLevel > 99;
    }

    @Override
    public String toString() {
        return String.format("%s%%; %s", batteryLevel, chargingState);
    }
}
