package com.cyanelix.chargetimer.electricity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

public class FlexibleOctopusTariff implements Tariff {
    private final ZoneId utcZone = ZoneId.of("UTC");

    private LocalTime dayStart = LocalTime.of(5, 30);
    private LocalTime nightStart = LocalTime.of(0, 30);

    private final float dayRate = 18.04f;
    private final float nightRate = 9.38f;

    @Override
    public List<RatePeriod> getRatePeriodsUntil(LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to)) {
            throw new TariffException("From date must be before to date");
        }

        ZonedDateTime rateStartDateTime = ZonedDateTime.of(from.toLocalDate(), dayStart, utcZone);

        ZonedDateTime rateEndDateTime;
        if (rateStartDateTime.toLocalTime().isAfter(nightStart)) {
            rateEndDateTime = ZonedDateTime.of()
        }
        ZonedDateTime rateEndDateTime = rateStartDateTime.with(TemporalAdjusters.next())

        return Arrays.asList(new RatePeriod(dayRate, ZonedDateTime.of(from.toLocalDate(), dayStart, utcZone),
                ZonedDateTime.of(to.toLocalDate(), nightStart, utcZone)));
    }
}
