package com.cyanelix.chargetimer.controller;

import com.cyanelix.chargetimer.octopus.OctopusClient;
import com.cyanelix.chargetimer.octopus.model.UnitRatesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "octopus")
public class TestOctopusController {
    private final OctopusClient octopusClient;

    @Autowired
    public TestOctopusController(OctopusClient octopusClient) {
        this.octopusClient = octopusClient;
    }

    @GetMapping
    public UnitRatesResponse getOctopusRates() {
        return octopusClient.getRatesFromNow();
    }
}
