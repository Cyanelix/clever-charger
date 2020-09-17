package com.cyanelix.chargetimer.electricity;

import java.time.LocalDateTime;
import java.util.List;

public interface Tariff {
    List<RatePeriod> getRatePeriodsBetween(LocalDateTime from, LocalDateTime to);
}
