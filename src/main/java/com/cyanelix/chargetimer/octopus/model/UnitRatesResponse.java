package com.cyanelix.chargetimer.octopus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;
import java.util.List;

public class UnitRatesResponse {
    @JsonProperty
    private List<UnitRate> results;

    public List<UnitRate> getResults() {
        results.sort(Comparator.comparing(UnitRate::getValidFrom));
        return results;
    }
}
