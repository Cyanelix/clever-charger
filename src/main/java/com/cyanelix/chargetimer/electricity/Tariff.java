package com.cyanelix.chargetimer.electricity;

import java.time.LocalDateTime;
import java.util.List;

public interface Tariff {
    List<RatePeriod> getRatePeriods(LocalDateTime from, LocalDateTime to);
}
