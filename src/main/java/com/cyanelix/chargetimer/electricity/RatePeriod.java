package com.cyanelix.chargetimer.electricity;

import java.time.ZonedDateTime;

public final class RatePeriod {
    private final float pence;
    private final ZonedDateTime start;
    private final ZonedDateTime end;

    public RatePeriod(float pence, ZonedDateTime start, ZonedDateTime end) {
        this.pence = pence;
        this.start = start;
        this.end = end;
    }

    public float getPence() {
        return pence;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }
}
