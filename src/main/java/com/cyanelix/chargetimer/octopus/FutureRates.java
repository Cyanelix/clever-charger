package com.cyanelix.chargetimer.octopus;

import com.cyanelix.chargetimer.octopus.model.UnitRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FutureRates {
    private final OctopusClient octopusClient;

    private List<UnitRate> unitRates;

    @Autowired
    public FutureRates(OctopusClient octopusClient) {
        this.octopusClient = octopusClient;
        cacheUnitRates();
    }

    @Scheduled(cron = "0 17 * * ?")
    public synchronized void cacheUnitRates() {
        unitRates = octopusClient.getRatesFromNow().getResults();
    }

    public List<UnitRate> getUnitRatesFromNow() {
        return unitRates;
    }
}
