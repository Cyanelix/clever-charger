package com.cyanelix.chargetimer.electricity;

import org.springframework.stereotype.Component;

@Component
public class TimeCalculator {
    // TODO: Make this variable? Calculate from charge available vs. full battery capacity?
    private static final float PERCENT_PER_MINUTE = 0.15f;

    public float secondsToChargePercent(int percent) {
        return (percent / PERCENT_PER_MINUTE) * 60f;
    }
}
