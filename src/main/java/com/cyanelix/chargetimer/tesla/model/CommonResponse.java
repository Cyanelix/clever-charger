package com.cyanelix.chargetimer.tesla.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommonResponse {
    @JsonProperty
    private boolean result;

    public boolean getResult() {
        return result;
    }
}
