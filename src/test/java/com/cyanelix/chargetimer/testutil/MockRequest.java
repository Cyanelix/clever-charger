package com.cyanelix.chargetimer.testutil;

import org.mockserver.model.HttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.Parameter.param;
import static org.mockserver.model.ParameterBody.params;

public class MockRequest {
    public static final String AUTH_TOKEN = "auth-token";

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

    public static HttpRequest getChargeState(Long id) {
        return request()
                .withMethod("GET")
                .withHeader("Authorization", "Bearer " + AUTH_TOKEN)
                .withPath("/api/1/vehicles/" + id + "/data_request/charge_state");
    }

    public static HttpRequest wakeUp(Long id) {
        return request()
                .withMethod(HttpMethod.POST.name())
                .withHeader(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_TOKEN)
                .withPath("/api/1/vehicles/" + id + "/wake_up");
    }
}
