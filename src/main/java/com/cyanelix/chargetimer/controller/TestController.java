package com.cyanelix.chargetimer.controller;

import com.cyanelix.chargetimer.tesla.TeslaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    private final TeslaClient teslaClient;

    @Autowired
    public TestController(TeslaClient teslaClient) {
        this.teslaClient = teslaClient;
    }

    @GetMapping("test")
    public String test() {
        return Integer.toString(teslaClient.getChargeState().getBatteryLevel());
    }
}
