package com.cyanelix.chargetimer.tesla;

import com.cyanelix.chargetimer.config.TeslaClientConfig;
import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.tesla.exception.TeslaClientException;
import com.cyanelix.chargetimer.tesla.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class TeslaClient {
    private final RestTemplate restTemplate;
    private final TeslaClientConfig teslaClientConfig;
    private final TeslaApiCache teslaApiCache;

    @Autowired
    public TeslaClient(RestTemplate restTemplate, TeslaClientConfig teslaClientConfig, TeslaApiCache teslaApiCache) {
        this.restTemplate = restTemplate;
        this.teslaClientConfig = teslaClientConfig;
        this.teslaApiCache = teslaApiCache;
    }

    public ChargeState getChargeState() {
        ChargeStateResponse response = makeRequest(createRequestEntity(),
                "/api/1/vehicles/{id}/data_request/charge_state", HttpMethod.GET,
                ChargeStateResponse.class);

        return response.getChargeState();
    }

    public boolean startCharging() {
        CommonResponse response =
                wakeAndRetryIfSleeping(() ->
                        makeRequest(createRequestEntity(),
                                "/api/1/vehicles/{id}/command/charge_start",
                                HttpMethod.POST, CommonResponse.class));

        return response.getResult();
    }

    public boolean stopCharging() {
        CommonResponse response =
                wakeAndRetryIfSleeping(() ->
                        makeRequest(createRequestEntity(),
                                "/api/1/vehicles/{id}/command/charge_stop",
                                HttpMethod.POST, CommonResponse.class));

        return response.getResult();
    }

    public boolean setChargeLimit(ChargeLevel chargeLevel) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.set("percent", Integer.toString(chargeLevel.getValue()));

        CommonResponse response =
                wakeAndRetryIfSleeping(() ->
                        makeRequest(createRequestEntity(body),
                                "/api/1/vehicles/{id}/command/set_charge_limit",
                                HttpMethod.POST, CommonResponse.class));

        return response.getResult();
    }

    private <T> T wakeAndRetryIfSleeping(Supplier<T> request) {
        try {
            return request.get();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.REQUEST_TIMEOUT)) {
                // Assume the car's asleep and ask it to wake up.
                Vehicle initialWake = wake().getVehicle();

                if (initialWake.getState().equals("asleep")) {
                    // We were right, the car was asleep.
                    // Wait for 30 seconds to give it a chance to wake up.
                    // This blocks, but that's OK, we don't expect it to happen often
                    // and if it does, we only expect to have a single thread running
                    // at a time anyway, so no issue for it to block.
                    try {
                        TimeUnit.SECONDS.sleep(teslaClientConfig.getWakeUpWaitSeconds());
                    } catch (InterruptedException e) {
                        // Do nothing.
                    }

                    // Call the wake_up endpoint again, if it's awake now retry the
                    // original request.
                    Vehicle wakeResult = wake().getVehicle();
                    if (wakeResult.getState().equals("online")) {
                        return request.get();
                    }
                }

                // Request timed-out but car wasn't asleep.
                throw new CarIsUnreachableException();
            }

            // Not a time-out, rethrow the original exception.
            throw ex;
        }
    }

    private VehicleResponse wake() {
        return makeRequest(createRequestEntity(),
                "/api/1/vehicles/{id}/wake_up", HttpMethod.POST, VehicleResponse.class);
    }

    private <B, R> R makeRequest(HttpEntity<B> requestEntity, String endpoint,
                                 HttpMethod method, Class<R> responseType) {
        if (!teslaApiCache.hasId()) {
            teslaApiCache.setId(getIdFromVin());
        }

        ResponseEntity<R> response = restTemplate.exchange(
                teslaClientConfig.getBaseUrl() + endpoint, method,
                requestEntity, responseType, teslaApiCache.getId());

        R body = response.getBody();
        if (body == null) {
            throw new TeslaClientException("No response received from endpoint on the Tesla API");
        }

        return body;
    }

    private String getAuthToken() {
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest(
                teslaClientConfig.getClientId(),
                teslaClientConfig.getClientSecret(),
                teslaClientConfig.getEmail(),
                teslaClientConfig.getPassword());

        AccessTokenResponse accessTokenResponse = restTemplate.postForObject(
                teslaClientConfig.getBaseUrl() + "/oauth/token",
                accessTokenRequest, AccessTokenResponse.class);

        if (accessTokenResponse == null) {
            throw new TeslaClientException(
                    "No response received from /oauth/token endpoint on the Tesla API");
        }

        return accessTokenResponse.getAccessToken();
    }

    private Long getIdFromVin() {
        HttpEntity<Void> request = createRequestEntity();

        HttpEntity<VehiclesResponse> vehiclesResponse = restTemplate.exchange(
                teslaClientConfig.getBaseUrl() + "/api/1/vehicles",
                HttpMethod.GET, request, VehiclesResponse.class);

        if (vehiclesResponse.getBody() == null) {
            throw new TeslaClientException(
                    "No response received from /api/1/vehicles endpoint on the Tesla API");
        }

        return vehiclesResponse.getBody().getVehicles().stream()
                .filter(vehicle -> vehicle.getVin().equals(teslaClientConfig.getVin()))
                .findAny()
                .map(Vehicle::getId)
                .orElseThrow(() -> new TeslaClientException("No vehicle with matching VIN found"));
    }

    private HttpEntity<Void> createRequestEntity() {
        return createRequestEntity(null);
    }

    private <T> HttpEntity<T> createRequestEntity(T body) {
        if (!teslaApiCache.hasAuthToken()) {
            teslaApiCache.setAuthToken(getAuthToken());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + teslaApiCache.getAuthToken());

        return new HttpEntity<>(body, headers);
    }
}
