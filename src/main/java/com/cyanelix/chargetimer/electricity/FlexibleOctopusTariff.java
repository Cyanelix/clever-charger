package com.cyanelix.chargetimer.electricity;

import java.time.LocalDateTime;
import java.util.List;

public class FlexibleOctopusTariff implements Tariff {
    private final float dayRate = 18.04f;

    @Override
    public List<RatePeriod> getRatePeriods(LocalDateTime from, LocalDateTime to) {
        return null;
    }
}
