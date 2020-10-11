package com.cyanelix.chargetimer.charges;

import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import com.cyanelix.chargetimer.microtypes.WeeklyTime;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class RequiredChargesRepository {
    private final Clock clock;

    private final SortedMap<WeeklyTime, ChargeLevel> weekly = new TreeMap<>();
    private SortedMap<ZonedDateTime, ChargeLevel> exceptions = new TreeMap<>();

    public RequiredChargesRepository(Clock clock) {
        this.clock = clock;
    }

    public RequiredCharge getNextRequiredCharge() {
        if (weekly.isEmpty() && exceptions.isEmpty()) {
            return null;
        }

        RequiredCharge nextWeeklyRequiredCharge =
                weekly.isEmpty() ? null : nextWeeklyRequiredCharge();

        RequiredCharge nextException =
                exceptions.isEmpty() ? null : nextException();

        if (nextException == null) {
            return nextWeeklyRequiredCharge;
        }

        if (nextWeeklyRequiredCharge == null) {
            return nextException;
        }

        if (nextWeeklyRequiredCharge.getRequiredBy().isBefore(
                nextException.getRequiredBy())) {
            return nextWeeklyRequiredCharge;
        }

        return nextException;
    }

    private RequiredCharge nextWeeklyRequiredCharge() {
        WeeklyTime weeklyTime = nextWeeklyKey();

        TemporalAdjuster dayAdjuster;
        if (weeklyTime.getLocalTime().isAfter(LocalTime.now(clock))) {
            dayAdjuster = TemporalAdjusters.nextOrSame(weeklyTime.getDayOfWeek());
        } else {
            dayAdjuster = TemporalAdjusters.next(weeklyTime.getDayOfWeek());
        }

        ZonedDateTime nextWeeklyTime = ZonedDateTime.now(clock)
                .with(dayAdjuster)
                .with(weeklyTime.getLocalTime());

        return RequiredCharge.of(weekly.get(weeklyTime), nextWeeklyTime);
    }

    private WeeklyTime nextWeeklyKey() {
        WeeklyTime now = WeeklyTime.now(clock);

        // Get all weekly schedules after now.
        SortedMap<WeeklyTime, ChargeLevel> tailMap = weekly.tailMap(now);

        if (!tailMap.isEmpty()) {
            return tailMap.firstKey();
        }

        // No weekly schedules after now must mean we're at the end of the week,
        // wrap around to the start.
        return weekly.firstKey();
    }

    private RequiredCharge nextException() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        exceptions = exceptions.tailMap(now);

        ZonedDateTime zonedDateTime = exceptions.firstKey();
        ChargeLevel chargeLevel = exceptions.get(zonedDateTime);

        return RequiredCharge.of(chargeLevel, zonedDateTime);
    }

    public void addException(RequiredCharge requiredCharge) {
        exceptions.put(requiredCharge.getRequiredBy(), requiredCharge.getChargeLevel());
    }

    public void addWeekly(WeeklyTime weeklyTime, ChargeLevel chargeLevel) {
        weekly.put(weeklyTime, chargeLevel);
    }
}
