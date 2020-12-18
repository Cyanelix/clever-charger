package com.cyanelix.chargetimer.controller;

import com.cyanelix.chargetimer.tesla.TeslaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "state")
public class StateController {
    private final TeslaClient teslaClient;

    @Autowired
    public StateController(TeslaClient teslaClient) {
        this.teslaClient = teslaClient;
    }

    @GetMapping("charge")
    public String getChargeState() {
        return Integer.toString(teslaClient.getChargeState().getBatteryLevel());
    }
}
