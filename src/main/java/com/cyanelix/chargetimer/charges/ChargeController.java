package com.cyanelix.chargetimer.charges;

import com.cyanelix.chargetimer.electricity.ChargeCalculator;
import com.cyanelix.chargetimer.electricity.RatePeriod;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import com.cyanelix.chargetimer.tesla.TeslaClient;
import com.cyanelix.chargetimer.tesla.model.ChargeState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class ChargeController {
    private final TeslaClient teslaClient;
    private final ChargeStateService chargeStateService;
    private final RequiredChargesRepository requiredChargesRepository;
    private final ChargeCalculator chargeCalculator;
    private final Clock clock;

    @Autowired
    public ChargeController(TeslaClient teslaClient, ChargeStateService chargeStateService, RequiredChargesRepository requiredChargesRepository, ChargeCalculator chargeCalculator, Clock clock) {
        this.teslaClient = teslaClient;
        this.chargeStateService = chargeStateService;
        this.requiredChargesRepository = requiredChargesRepository;
        this.chargeCalculator = chargeCalculator;
        this.clock = clock;
    }

    // TODO: need to schedule this
    public void chargeIfNeeded() {
        ChargeState chargeState = chargeStateService.getChargeState();
        if (chargeState.isUnplugged() || chargeState.isFullyCharged()) {
            // Car's unplugged or full; nothing we can do for now.
            return;
        }

        RequiredCharge nextRequiredCharge = requiredChargesRepository.getNextRequiredCharge();
        if (nextRequiredCharge == null) {
            // We don't have any required charges; do nothing and check again in a while.
            return;
        }

        RatePeriod nextChargePeriod = chargeCalculator.getNextChargePeriod(
                nextRequiredCharge, chargeState.getChargeLevel());

        // TODO: Might need to wake up the car in either of these cases.
        if (nextChargePeriod.chargeNow(clock) && chargeState.isReadyToCharge()) {
            teslaClient.setChargeLimit(nextRequiredCharge.getChargeLevel());
            teslaClient.startCharging();
        } else if (!nextChargePeriod.chargeNow(clock) && chargeState.isCharging()) {
            teslaClient.stopCharging();
        }
    }
}
