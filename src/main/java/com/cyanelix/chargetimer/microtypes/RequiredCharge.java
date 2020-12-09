package com.cyanelix.chargetimer.microtypes;

import java.time.ZonedDateTime;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequiredCharge that = (RequiredCharge) o;
        return chargeLevel.equals(that.chargeLevel) &&
                requiredBy.equals(that.requiredBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chargeLevel, requiredBy);
    }

    @Override
    public String toString() {
        return String.format("Requiring %s @ %s", chargeLevel, requiredBy);
    }
}
