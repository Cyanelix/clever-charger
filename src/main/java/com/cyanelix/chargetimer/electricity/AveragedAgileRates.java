package com.cyanelix.chargetimer.electricity;

import com.cyanelix.chargetimer.octopus.model.UnitRate;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AveragedAgileRates {
    private final Map<LocalTime, BigDecimal> averageRates;

    public AveragedAgileRates() {
        averageRates = new HashMap<>(48);

        averageRates.put(LocalTime.parse("00:00"), new BigDecimal("8.037522973"));
        averageRates.put(LocalTime.parse("00:30"), new BigDecimal("8.667239189"));
        averageRates.put(LocalTime.parse("01:00"), new BigDecimal("8.402071622"));
        averageRates.put(LocalTime.parse("01:30"), new BigDecimal("7.65135"));
        averageRates.put(LocalTime.parse("02:00"), new BigDecimal("7.786317568"));
        averageRates.put(LocalTime.parse("02:30"), new BigDecimal("7.3836"));
        averageRates.put(LocalTime.parse("03:00"), new BigDecimal("7.612414865"));
        averageRates.put(LocalTime.parse("03:30"), new BigDecimal("7.190683784"));
        averageRates.put(LocalTime.parse("04:00"), new BigDecimal("7.817391892"));
        averageRates.put(LocalTime.parse("04:30"), new BigDecimal("7.797612162"));
        averageRates.put(LocalTime.parse("05:00"), new BigDecimal("8.908625676"));
        averageRates.put(LocalTime.parse("05:30"), new BigDecimal("8.91555"));
        averageRates.put(LocalTime.parse("06:00"), new BigDecimal("9.498158108"));
        averageRates.put(LocalTime.parse("06:30"), new BigDecimal("10.38001622"));
        averageRates.put(LocalTime.parse("07:00"), new BigDecimal("10.58365946"));
        averageRates.put(LocalTime.parse("07:30"), new BigDecimal("11.10338108"));
        averageRates.put(LocalTime.parse("08:00"), new BigDecimal("11.25458108"));
        averageRates.put(LocalTime.parse("08:30"), new BigDecimal("11.02894459"));
        averageRates.put(LocalTime.parse("09:00"), new BigDecimal("11.1387973"));
        averageRates.put(LocalTime.parse("09:30"), new BigDecimal("10.60116892"));
        averageRates.put(LocalTime.parse("10:00"), new BigDecimal("10.56666081"));
        averageRates.put(LocalTime.parse("10:30"), new BigDecimal("10.1153027"));
        averageRates.put(LocalTime.parse("11:00"), new BigDecimal("10.11419595"));
        averageRates.put(LocalTime.parse("11:30"), new BigDecimal("9.748654054"));
        averageRates.put(LocalTime.parse("12:00"), new BigDecimal("9.714117568"));
        averageRates.put(LocalTime.parse("12:30"), new BigDecimal("9.27465"));
        averageRates.put(LocalTime.parse("13:00"), new BigDecimal("9.25365"));
        averageRates.put(LocalTime.parse("13:30"), new BigDecimal("9.061074324"));
        averageRates.put(LocalTime.parse("14:00"), new BigDecimal("9.006048649"));
        averageRates.put(LocalTime.parse("14:30"), new BigDecimal("9.245760811"));
        averageRates.put(LocalTime.parse("15:00"), new BigDecimal("15.73527162"));
        averageRates.put(LocalTime.parse("15:30"), new BigDecimal("17.21256486"));
        averageRates.put(LocalTime.parse("16:00"), new BigDecimal("22.30270946"));
        averageRates.put(LocalTime.parse("16:30"), new BigDecimal("24.23689459"));
        averageRates.put(LocalTime.parse("17:00"), new BigDecimal("24.45679865"));
        averageRates.put(LocalTime.parse("17:30"), new BigDecimal("25.30102703"));
        averageRates.put(LocalTime.parse("18:00"), new BigDecimal("18.51981486"));
        averageRates.put(LocalTime.parse("18:30"), new BigDecimal("17.96907568"));
        averageRates.put(LocalTime.parse("19:00"), new BigDecimal("12.67270541"));
        averageRates.put(LocalTime.parse("19:30"), new BigDecimal("11.62605405"));
        averageRates.put(LocalTime.parse("20:00"), new BigDecimal("11.40254595"));
        averageRates.put(LocalTime.parse("20:30"), new BigDecimal("10.10826486"));
        averageRates.put(LocalTime.parse("21:00"), new BigDecimal("10.08652703"));
        averageRates.put(LocalTime.parse("21:30"), new BigDecimal("9.00777973"));
        averageRates.put(LocalTime.parse("22:00"), new BigDecimal("9.518704054"));
        averageRates.put(LocalTime.parse("22:30"), new BigDecimal("8.996939189"));
        averageRates.put(LocalTime.parse("23:00"), new BigDecimal("8.848654472"));
        averageRates.put(LocalTime.parse("23:30"), new BigDecimal("8.18553252"));
    }

    public List<UnitRate> getAverageRatesBetween(ZonedDateTime from, ZonedDateTime to) {
        List<UnitRate> rates = new ArrayList<>();

        // Round the from time down to the nearest half-hour.
        ZonedDateTime periodStart = from.truncatedTo(ChronoUnit.HOURS)
                .plusMinutes(30 * (from.getMinute() / 30));

        while (periodStart.isBefore(to)) {
            BigDecimal rate = averageRates.get(periodStart.toLocalTime());

            ZonedDateTime periodEnd = periodStart.plusMinutes(30);
            rates.add(new UnitRate(rate, periodStart, periodEnd));

            periodStart = periodEnd;
        }

        return rates;
    }
}
