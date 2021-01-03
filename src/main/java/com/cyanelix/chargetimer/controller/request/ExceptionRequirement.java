package com.cyanelix.chargetimer.controller.request;

import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public final class ExceptionRequirement {
    private final ZonedDateTime dateTime;
    private final ChargeLevel chargeLevel;

    @JsonCreator
    public ExceptionRequirement(
            @JsonProperty String dateTime,
            @JsonProperty int chargeLevel) {
        this.dateTime = ZonedDateTime.parse(dateTime);
        this.chargeLevel = ChargeLevel.of(chargeLevel);
    }

    public RequiredCharge asRequiredCharge() {
        return RequiredCharge.of(chargeLevel, dateTime);
    }
}
