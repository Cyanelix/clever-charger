package com.cyanelix.chargetimer.tesla.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VehicleResponse {
    @JsonProperty("response")
    private Vehicle vehicle;

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}
