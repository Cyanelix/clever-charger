package com.cyanelix.chargetimer.octopus;

import com.cyanelix.chargetimer.octopus.model.UnitRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FutureRates {
    private static final Logger LOG = LoggerFactory.getLogger(FutureRates.class);

    private final OctopusClient octopusClient;
    private final Clock clock;

    private List<UnitRate> unitRates;

    @Autowired
    public FutureRates(OctopusClient octopusClient, Clock clock) {
        this.octopusClient = octopusClient;
        this.clock = clock;
    }

    @Scheduled(cron = "0 30 * * * ?")
    public synchronized void cacheUnitRates() {
        LOG.debug("Getting rates from Octopus");
        unitRates = octopusClient.getRatesFromNow().getResults();
        LOG.debug("Retrieved {} periods", unitRates.size());
    }

    public List<UnitRate> getUnitRatesFromNow() {
        ZonedDateTime now = ZonedDateTime.now(clock);

        if (unitRates == null) {
            cacheUnitRates();
        }

        return unitRates.stream()
                .filter(unitRate -> unitRate.getValidTo().isAfter(now))
                .collect(Collectors.toList());
    }
}
