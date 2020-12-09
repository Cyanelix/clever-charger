package com.cyanelix.chargetimer.testutil;

import org.mockserver.model.HttpRequest;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.Parameter.param;
import static org.mockserver.model.ParameterBody.params;

public class MockRequest {
    public static HttpRequest stopCharging(Long id) {
        return request().withPath("/api/1/vehicles/" + id + "/command/charge_stop");
    }

    public static HttpRequest startCharging(Long id) {
        return request().withPath("/api/1/vehicles/" + id + "/command/charge_start");
    }

    public static HttpRequest setChargeLimit(Long id, int chargeLimit) {
        return request().withPath("/api/1/vehicles/" + id + "/command/set_charge_limit")
                .withBody(params(param("percent", Integer.toString(chargeLimit))));
    }

    public static HttpRequest setAnyChargeLimit(Long id) {
        return request().withPath("/api/1/vehicles/" + id + "/command/set_charge_limit");
    }
}
