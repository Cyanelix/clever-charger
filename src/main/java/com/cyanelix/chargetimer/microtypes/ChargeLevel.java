package com.cyanelix.chargetimer.microtypes;

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

    public boolean exceeds(ChargeLevel that) {
        return this.getValue() > that.getValue();
    }

    @Override
    public String toString() {
        return value + "%";
    }
}
