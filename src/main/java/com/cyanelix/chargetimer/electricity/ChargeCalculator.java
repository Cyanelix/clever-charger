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
    private final TimeCalculator timeCalculator;
    private final Tariff tariff;
    private final Clock clock;

    @Autowired
    public ChargeCalculator(TimeCalculator timeCalculator, Tariff tariff, Clock clock) {
        this.timeCalculator = timeCalculator;
        this.tariff = tariff;
        this.clock = clock;
    }

    public RatePeriod getNextChargePeriod(RequiredCharge requiredCharge, ChargeLevel currentCharge) {
        // TODO: Do I want this? It means that if I need 20% in 1 hour, but 100% in 3 hours, it will stop at 20% for an hour
        if (currentCharge.equalsOrExceeds(requiredCharge.getChargeLevel())) {
            return RatePeriod.NULL_RATE_PERIOD;
        }

        int percentRequired = requiredCharge.getChargeLevel().getValue() - currentCharge.getValue();
        float numberOfSeconds = timeCalculator.secondsToChargePercent(percentRequired);

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
