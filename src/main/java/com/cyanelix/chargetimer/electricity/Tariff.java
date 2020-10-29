package com.cyanelix.chargetimer.electricity;

import java.time.ZonedDateTime;
import java.util.List;

public interface Tariff {
    List<PricedRatePeriod> getRatePeriodsBetween(ZonedDateTime from, ZonedDateTime to);
}
