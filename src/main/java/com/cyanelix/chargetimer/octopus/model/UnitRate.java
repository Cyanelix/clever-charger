package com.cyanelix.chargetimer.octopus.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UnitRate {
    private BigDecimal valueIncVat;
    private ZonedDateTime validFrom;
    private ZonedDateTime validTo;

    public BigDecimal getValueIncVat() {
        return valueIncVat;
    }

    public ZonedDateTime getValidFrom() {
        return validFrom;
    }

    public ZonedDateTime getValidTo() {
        return validTo;
    }
}
