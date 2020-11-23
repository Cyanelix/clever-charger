package com.cyanelix.chargetimer.tesla;

import com.cyanelix.chargetimer.config.TeslaClientConfig;
import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.tesla.exception.TeslaClientException;
import com.cyanelix.chargetimer.tesla.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
        ChargeStateResponse response = makeRequest(createRequestEntity(),
                "/api/1/vehicles/{id}/data_request/charge_state", HttpMethod.GET,
                ChargeStateResponse.class);

        return response.getChargeState();
    }

    public boolean startCharging() {
        CommonResponse response = makeRequest(createRequestEntity(),
                "/api/1/vehicles/{id}/command/charge_start", HttpMethod.POST,
                CommonResponse.class);

        return response.getResult();
    }

    public boolean stopCharging() {
        CommonResponse response = makeRequest(createRequestEntity(),
                "/api/1/vehicles/{id}/command/charge_stop", HttpMethod.POST,
                CommonResponse.class);

        return response.getResult();
    }

    public boolean setChargeLimit(ChargeLevel chargeLevel) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.set("percent", Integer.toString(chargeLevel.getValue()));

        CommonResponse response = makeRequest(createRequestEntity(body),
                "/api/1/vehicles/{id}/command/set_charge_limit", HttpMethod.POST,
                CommonResponse.class);

        return response.getResult();
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
