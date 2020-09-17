package com.cyanelix.chargetimer.electricity;

import java.time.LocalDateTime;
import java.util.List;

public interface Tariff {
    List<RatePeriod> getRatePeriodsUntil(LocalDateTime from, LocalDateTime to);
}
