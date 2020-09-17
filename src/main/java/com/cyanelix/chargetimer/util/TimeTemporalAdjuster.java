package com.cyanelix.chargetimer.util;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

public class TimeTemporalAdjuster implements TemporalAdjuster {
    private final LocalTime targetTime;

    public TimeTemporalAdjuster(LocalTime targetTime) {
        this.targetTime = targetTime;
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        LocalTime localTime = LocalTime.from(temporal);
        if (localTime.isAfter(targetTime)) {
            return temporal.plus(Duration.ofHours(24).minus(Duration.between(targetTime, localTime)));
        } else if (localTime.isBefore(targetTime)) {
            return temporal.plus(Duration.between(localTime, targetTime));
        } else {
            return temporal.plus(Duration.ofHours(24));
        }
    }
}
