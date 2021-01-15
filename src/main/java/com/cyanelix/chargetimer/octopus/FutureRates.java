package com.cyanelix.chargetimer.octopus;

import com.cyanelix.chargetimer.octopus.model.UnitRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FutureRates {
    private static final Logger LOG = LoggerFactory.getLogger(FutureRates.class);

    private final OctopusClient octopusClient;

    private List<UnitRate> unitRates;

    @Autowired
    public FutureRates(OctopusClient octopusClient) {
        this.octopusClient = octopusClient;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public synchronized void cacheUnitRates() {
        LOG.debug("Getting rates from Octopus");
        unitRates = octopusClient.getRatesFromNow().getResults();
        LOG.debug("Retrieved {} periods", unitRates.size());
    }

    public List<UnitRate> getUnitRatesFromNow() {
        if (unitRates == null) {
            cacheUnitRates();
        }
        return unitRates;
    }
}
