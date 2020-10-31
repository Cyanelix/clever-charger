package com.cyanelix.chargetimer.charges;

import com.cyanelix.chargetimer.electricity.ChargeCalculator;
import com.cyanelix.chargetimer.electricity.RatePeriod;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import com.cyanelix.chargetimer.tesla.TeslaClient;
import com.cyanelix.chargetimer.tesla.model.ChargeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Clock;

@Component
public class ChargeController {
    private final static Logger LOG = LoggerFactory.getLogger(ChargeController.class);

    private final TeslaClient teslaClient;
    private final RequiredChargesRepository requiredChargesRepository;
    private final ChargeCalculator chargeCalculator;
    private final Clock clock;

    @Autowired
    public ChargeController(TeslaClient teslaClient, RequiredChargesRepository requiredChargesRepository, ChargeCalculator chargeCalculator, Clock clock) {
        this.teslaClient = teslaClient;
        this.requiredChargesRepository = requiredChargesRepository;
        this.chargeCalculator = chargeCalculator;
        this.clock = clock;
    }

    // TODO: need to schedule this
    public void chargeIfNeeded() {
        ChargeState chargeState = getChargeState();
        if (chargeState == null || chargeState.isUnplugged()) {
            // Car's out of range or unplugged; nothing we can do for now.
            return;
        }

        RequiredCharge nextRequiredCharge = requiredChargesRepository.getNextRequiredCharge();
        if (nextRequiredCharge == null) {
            // We don't have any required charges; do nothing and check again in a while.
            return;
        }

        teslaClient.setChargeLimit(nextRequiredCharge.getChargeLevel());

        RatePeriod nextChargePeriod = chargeCalculator.getNextChargePeriod(
                nextRequiredCharge, chargeState.getChargeLevel());

        if (nextChargePeriod.chargeNow(clock) && chargeState.isReadyToCharge()) {
            teslaClient.startCharging();
        } else if (!nextChargePeriod.chargeNow(clock) && chargeState.isCharging()) {
            teslaClient.stopCharging();
        }
    }

    private ChargeState getChargeState() {
        try {
            return teslaClient.getChargeState();
        } catch (HttpClientErrorException ex) {
            LOG.warn("Client error when requesting charge state", ex);
        }

        return null;
    }
}
