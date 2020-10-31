package com.cyanelix.chargetimer.tesla;

import com.cyanelix.chargetimer.config.TeslaClientConfig;
import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.tesla.model.*;
import com.cyanelix.chargetimer.tesla.exception.TeslaClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
        if (!teslaApiCache.hasId()) {
            teslaApiCache.setId(getIdFromVin());
        }

        HttpEntity<Void> requestEntity = createRequestEntity();

        ResponseEntity<ChargeStateResponse> chargeStateResponse = restTemplate.exchange(
                teslaClientConfig.getBaseUrl() + "/api/1/vehicles/{id}/data_request/charge_state",
                HttpMethod.GET, requestEntity, ChargeStateResponse.class, teslaApiCache.getId());

        if (chargeStateResponse.getBody() == null) {
            throw new TeslaClientException(
                    "No response received from charge_state endpoint on the Tesla API");
        }

        return chargeStateResponse.getBody().getChargeState();
    }

    public void startCharging() {
        // TODO
    }

    public void stopCharging() {
        // TODO
    }

    public void setChargeLimit(ChargeLevel chargeLevel) {
        // TODO
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
