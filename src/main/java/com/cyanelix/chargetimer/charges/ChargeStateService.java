package com.cyanelix.chargetimer.charges;

import com.cyanelix.chargetimer.tesla.TeslaClient;
import com.cyanelix.chargetimer.tesla.model.ChargeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

@Service
public class ChargeStateService {
    private static final Logger LOG = LoggerFactory.getLogger(ChargeStateService.class);

    private final TeslaClient teslaClient;

    private ChargeState lastKnownChargeState;

    public ChargeStateService(TeslaClient teslaClient) {
        this.teslaClient = teslaClient;

        lastKnownChargeState = new ChargeState();
        lastKnownChargeState.setBatteryLevel(0);
        lastKnownChargeState.setChargingState("Unknown");
    }

    public ChargeState getChargeState() {
        try {
            ChargeState chargeState = teslaClient.getChargeState();

            lastKnownChargeState = chargeState;

            return chargeState;
        } catch (HttpStatusCodeException ex) {
            LOG.warn("HTTP error when requesting charge state", ex);
        }

        ChargeState unreachableChargeState = new ChargeState();
        unreachableChargeState.setBatteryLevel(lastKnownChargeState.getBatteryLevel());
        unreachableChargeState.setChargingState("Unknown");

        return unreachableChargeState;
    }
}
