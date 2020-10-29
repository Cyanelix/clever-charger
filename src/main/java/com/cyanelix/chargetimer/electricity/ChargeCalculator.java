package com.cyanelix.chargetimer.electricity;

import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class ChargeCalculator {
    // TODO: Make this variable? Calculate from charge available vs. full battery capacity?
    private static final float PERCENT_PER_MINUTE = 0.16f;

    private final Tariff tariff;
    private final Clock clock;

    @Autowired
    public ChargeCalculator(Tariff tariff, Clock clock) {
        this.tariff = tariff;
        this.clock = clock;
    }

    public RatePeriod getNextChargePeriod(RequiredCharge requiredCharge, ChargeLevel currentCharge) {
        if (currentCharge.equalsOrExceeds(requiredCharge.getChargeLevel())) {
            return RatePeriod.NULL_RATE_PERIOD;
        }

        int percentRequired = requiredCharge.getChargeLevel().getValue() - currentCharge.getValue();
        float numberOfSeconds = (percentRequired / PERCENT_PER_MINUTE) * 60f;

        List<PricedRatePeriod> ratePeriods = tariff.getRatePeriodsBetween(
                ZonedDateTime.now(clock), requiredCharge.getRequiredBy());

        if (ratePeriods.size() == 0) {
            return RatePeriod.NULL_RATE_PERIOD;
        }

        ratePeriods.sort(
                Comparator.comparing(PricedRatePeriod::getPence)
                        .thenComparing(PricedRatePeriod::getStart));

        List<PricedRatePeriod> selectedPeriods = new ArrayList<>();

        while (totalSeconds(selectedPeriods) < (int) numberOfSeconds
                && selectedPeriods.size() < ratePeriods.size()) {
            selectedPeriods = ratePeriods.subList(0, selectedPeriods.size() + 1);
        }

        selectedPeriods.sort(Comparator.comparing(PricedRatePeriod::getStart));

        return selectedPeriods.get(0);
    }

    private long totalSeconds(List<? extends RatePeriod> ratePeriods) {
        return ratePeriods.stream()
                .map(RatePeriod::getSeconds)
                .reduce(Long::sum)
                .orElse(0L);
    }
}
