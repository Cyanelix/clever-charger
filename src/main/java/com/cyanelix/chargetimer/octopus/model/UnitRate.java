package com.cyanelix.chargetimer.octopus.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UnitRate {
    private final BigDecimal valueIncVat;
    private final ZonedDateTime validFrom;
    private final ZonedDateTime validTo;

    @JsonCreator
    public UnitRate(
            @JsonProperty("value_inc_vat") BigDecimal valueIncVat,
            @JsonProperty("valid_from") ZonedDateTime validFrom,
            @JsonProperty("valid_to") ZonedDateTime validTo) {
        this.valueIncVat = valueIncVat;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public BigDecimal getValueIncVat() {
        return valueIncVat;
    }

    public ZonedDateTime getValidFrom() {
        return validFrom;
    }

    public ZonedDateTime getValidTo() {
        return validTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnitRate unitRate = (UnitRate) o;
        return Objects.equals(valueIncVat, unitRate.valueIncVat) && Objects.equals(validFrom, unitRate.validFrom) && Objects.equals(validTo, unitRate.validTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueIncVat, validFrom, validTo);
    }

    @Override
    public String toString() {
        return "UnitRate{" +
                "valueIncVat=" + valueIncVat +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                '}';
    }
}
