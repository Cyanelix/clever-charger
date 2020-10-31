package com.cyanelix.chargetimer.microtypes;

import java.util.Objects;

public final class ChargeLevel {
    private final int value;

    private ChargeLevel(int value) {
        this.value = value;
    }

    public static ChargeLevel of(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Charge level must be at least 0");
        }

        if (value > 100) {
            throw new IllegalArgumentException("Charge level must be no more than 100");
        }

        return new ChargeLevel(value);
    }

    public int getValue() {
        return value;
    }

    public boolean equalsOrExceeds(ChargeLevel that) {
        return this.getValue() >= that.getValue();
    }

    @Override
    public String toString() {
        return value + "%";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChargeLevel that = (ChargeLevel) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
