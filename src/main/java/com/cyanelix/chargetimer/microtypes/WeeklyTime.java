package com.cyanelix.chargetimer.microtypes;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;

public final class WeeklyTime implements Comparable<WeeklyTime> {
    private static final Comparator<WeeklyTime> NATURAL_ORDER_COMPARATOR =
            Comparator.comparing(WeeklyTime::getDayOfWeek)
                    .thenComparing(WeeklyTime::getLocalTime);

    private final DayOfWeek dayOfWeek;
    private final LocalTime localTime;

    public WeeklyTime(DayOfWeek dayOfWeek, LocalTime localTime) {
        this.dayOfWeek = dayOfWeek;
        this.localTime = localTime;
    }

    public static WeeklyTime now(Clock clock) {
        ZonedDateTime now = ZonedDateTime.now(clock);
        return new WeeklyTime(now.getDayOfWeek(), now.toLocalTime());
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    @Override
    public int compareTo(WeeklyTime that) {
        return NATURAL_ORDER_COMPARATOR.compare(this, that);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeeklyTime that = (WeeklyTime) o;
        return dayOfWeek == that.dayOfWeek && localTime.equals(that.localTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, localTime);
    }

    @Override
    public String toString() {
        return dayOfWeek + " @ " + localTime;
    }
}
