package com.cyanelix.chargetimer.tesla.model;

import com.cyanelix.chargetimer.tesla.model.Vehicle;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class VehiclesResponse {
    @JsonProperty("response")
    private List<Vehicle> vehicles;

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }
}
