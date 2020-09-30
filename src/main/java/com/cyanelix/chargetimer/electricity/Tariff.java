package com.cyanelix.chargetimer.electricity;

import java.time.ZonedDateTime;
import java.util.List;

public interface Tariff {
    List<RatePeriod> getRatePeriodsBetween(ZonedDateTime from, ZonedDateTime to);
}
