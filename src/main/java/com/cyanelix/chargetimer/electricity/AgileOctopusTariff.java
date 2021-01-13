package com.cyanelix.chargetimer.electricity;

import com.cyanelix.chargetimer.octopus.AveragedAgileRates;
import com.cyanelix.chargetimer.octopus.FutureRates;
import com.cyanelix.chargetimer.octopus.model.UnitRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AgileOctopusTariff implements Tariff {
    private final FutureRates futureRates;
    private final AveragedAgileRates averagedAgileRates;

    @Autowired
    public AgileOctopusTariff(FutureRates futureRates, AveragedAgileRates averagedAgileRates) {
        this.futureRates = futureRates;
        this.averagedAgileRates = averagedAgileRates;
    }

    // TODO Test this!
    @Override
    public List<PricedRatePeriod> getRatePeriodsBetween(ZonedDateTime from, ZonedDateTime to) {
        List<UnitRate> futureUnitRates = futureRates.getUnitRatesFromNow();
        List<UnitRate> unitRates = getUnitRates(from, to, futureUnitRates);

        return unitRates.stream()
                .map(unitRate -> new PricedRatePeriod(
                        unitRate.getValueIncVat().floatValue(),
                        unitRate.getValidFrom(),
                        unitRate.getValidTo()))
                .collect(Collectors.toList());
    }

    private List<UnitRate> getUnitRates(ZonedDateTime from, ZonedDateTime to, List<UnitRate> futureUnitRates) {
        if (futureUnitRates.isEmpty()) {
            return averagedAgileRates.getAverageRatesBetween(from, to);
        }

        ZonedDateTime firstFuturePeriodStart = futureUnitRates.get(0).getValidFrom();
        ZonedDateTime lastFuturePeriodEnd = futureUnitRates.get(futureUnitRates.size() - 1).getValidTo();

        List<UnitRate> unitRates;
        if (!to.isAfter(firstFuturePeriodStart) || !from.isBefore(lastFuturePeriodEnd)) {
            // if to <= future start: just get averaged rates between from and to
            // or from >= future end: just get averaged rates between from and to
            unitRates = averagedAgileRates.getAverageRatesBetween(from, to);
        } else {
            unitRates = new ArrayList<>();

            // if from < future start, get averaged rates between from and future start
            if (from.isBefore(firstFuturePeriodStart)) {
                unitRates.addAll(averagedAgileRates.getAverageRatesBetween(from, firstFuturePeriodStart));
            }

            // one-by-one add futures until to or until all futures added
            ZonedDateTime pointer = firstFuturePeriodStart;
            int futureCounter = 0;
            while (pointer.isBefore(to) && futureCounter < futureUnitRates.size()) {
                UnitRate futureRate = futureUnitRates.get(futureCounter++);
                unitRates.add(futureRate);
                pointer = futureRate.getValidTo();
            }

            // if to > future end, add averaged rates between future end and to
            if (to.isAfter(lastFuturePeriodEnd)) {
                unitRates.addAll(averagedAgileRates.getAverageRatesBetween(lastFuturePeriodEnd, to));
            }
        }
        return unitRates;
    }
}
