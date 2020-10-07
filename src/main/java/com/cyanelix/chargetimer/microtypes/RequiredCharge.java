package com.cyanelix.chargetimer.microtypes;

import java.time.ZonedDateTime;

public final class RequiredCharge {
    private final ChargeLevel chargeLevel;
    private final ZonedDateTime requiredBy;

    private RequiredCharge(ChargeLevel chargeLevel, ZonedDateTime requiredBy) {
        this.chargeLevel = chargeLevel;
        this.requiredBy = requiredBy;
    }

    public static RequiredCharge of(ChargeLevel chargeLevel, ZonedDateTime requiredBy) {
        return new RequiredCharge(chargeLevel, requiredBy);
    }

    public ChargeLevel getChargeLevel() {
        return chargeLevel;
    }

    public ZonedDateTime getRequiredBy() {
        return requiredBy;
    }
}
