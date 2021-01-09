package com.cyanelix.chargetimer.electricity;

import java.time.ZonedDateTime;
import java.util.List;

public class AgileOctopusTariff implements Tariff {
    @Override
    public List<PricedRatePeriod> getRatePeriodsBetween(ZonedDateTime from, ZonedDateTime to) {
        return null;
    }
}
