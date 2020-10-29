package com.cyanelix.chargetimer.electricity;

import com.cyanelix.chargetimer.util.TimeTemporalAdjuster;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.util.ArrayList;
import java.util.List;

public class FlexibleOctopusTariff implements Tariff {
    private final LocalTime nightStart = LocalTime.of(0, 30);
    private final LocalTime dayStart = LocalTime.of(5, 30);

    private final TemporalAdjuster nextNightStartAdjuster = new TimeTemporalAdjuster(nightStart);
    private final TemporalAdjuster nextDayStartAdjuster = new TimeTemporalAdjuster(dayStart);

    private final float dayRate = 18.04f;
    private final float nightRate = 9.38f;

    @Override
    public List<PricedRatePeriod> getRatePeriodsBetween(ZonedDateTime from, ZonedDateTime to) {
        if (from.isAfter(to)) {
            throw new TariffException("From date must be before to date");
        }

        PricedRatePeriod firstRatePeriod;

        if (from.toLocalTime().isBefore(nightStart)) {
            ZonedDateTime previousDayPeriod = atTime(from.minusDays(1), dayStart);
            firstRatePeriod = new PricedRatePeriod(dayRate, previousDayPeriod,
                    previousDayPeriod.with(nextNightStartAdjuster));
        } else if (from.toLocalTime().isBefore(dayStart)) {
            ZonedDateTime startNightPeriod = atTime(from, nightStart);
            firstRatePeriod = new PricedRatePeriod(nightRate, startNightPeriod,
                    startNightPeriod.with(nextDayStartAdjuster));
        } else {
            ZonedDateTime startDayPeriod = atTime(from, dayStart);
            firstRatePeriod = new PricedRatePeriod(dayRate, startDayPeriod,
                    startDayPeriod.with(nextNightStartAdjuster));
        }

        List<PricedRatePeriod> ratePeriods = new ArrayList<>();
        ratePeriods.add(firstRatePeriod);

        while (ratePeriods.get(ratePeriods.size() - 1).getEnd().isBefore(to)) {
            ratePeriods.add(getNextRatePeriod(ratePeriods.get(ratePeriods.size() - 1)));
        }

        return ratePeriods;
    }

    private ZonedDateTime atTime(ZonedDateTime zonedDateTime, LocalTime localTime) {
        return zonedDateTime.withHour(localTime.getHour())
                .withMinute(localTime.getMinute())
                .withSecond(localTime.getSecond())
                .withNano(localTime.getNano());
    }

    private PricedRatePeriod getNextRatePeriod(PricedRatePeriod previousRatePeriod) {
        ZonedDateTime previousEnd = previousRatePeriod.getEnd();

        if (previousRatePeriod.getPence() == dayRate) {
            ZonedDateTime nextEnd = previousEnd.with(nextDayStartAdjuster);
            return new PricedRatePeriod(nightRate, previousEnd, nextEnd);
        }

        ZonedDateTime nextEnd = previousEnd.with(nextNightStartAdjuster);
        return new PricedRatePeriod(dayRate, previousEnd, nextEnd);
    }
}
