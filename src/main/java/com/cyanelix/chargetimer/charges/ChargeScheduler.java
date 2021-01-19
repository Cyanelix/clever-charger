package com.cyanelix.chargetimer.charges;

import com.cyanelix.chargetimer.electricity.ChargeCalculator;
import com.cyanelix.chargetimer.electricity.RatePeriod;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import com.cyanelix.chargetimer.tesla.TeslaClient;
import com.cyanelix.chargetimer.tesla.model.ChargeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class ChargeScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(ChargeScheduler.class);

    private final TeslaClient teslaClient;
    private final ChargeStateService chargeStateService;
    private final RequiredChargesRepository requiredChargesRepository;
    private final ChargeCalculator chargeCalculator;
    private final Clock clock;

    @Autowired
    public ChargeScheduler(TeslaClient teslaClient, ChargeStateService chargeStateService, RequiredChargesRepository requiredChargesRepository, ChargeCalculator chargeCalculator, Clock clock) {
        this.teslaClient = teslaClient;
        this.chargeStateService = chargeStateService;
        this.requiredChargesRepository = requiredChargesRepository;
        this.chargeCalculator = chargeCalculator;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 120000L)
    public void chargeIfNeeded() {
        ChargeState chargeState = chargeStateService.getChargeState();

        LOG.debug("Current state: {}", chargeState);

        if (chargeState.isUnplugged() || chargeState.isFullyCharged()) {
            // Car's unplugged or full; nothing we can do for now.
            LOG.debug("Nothing to do, charge state: {}", chargeState);
            return;
        }

        RequiredCharge nextRequiredCharge = requiredChargesRepository.getNextRequiredCharge();
        if (nextRequiredCharge == null) {
            // We don't have any required charges; do nothing and check again in a while.
            LOG.debug("Nothing to do, no required charges");
            return;
        }

        LOG.debug("Next required charge: {}", nextRequiredCharge);

        RatePeriod nextChargePeriod = chargeCalculator.getNextChargePeriod(
                nextRequiredCharge, chargeState.getChargeLevel());

        LOG.debug("Next charge period: {}", nextChargePeriod);

        if (nextChargePeriod.chargeNow(clock)
                && (chargeState.isReadyToCharge() || chargeState.isUnknown())) {
            LOG.debug("Starting charging");
            teslaClient.setChargeLimit(nextRequiredCharge.getChargeLevel());
            teslaClient.startCharging();
        } else if (!nextChargePeriod.chargeNow(clock) && chargeState.isCharging()) {
            LOG.debug("Stopping charging");
            teslaClient.stopCharging();
        }
    }
}
