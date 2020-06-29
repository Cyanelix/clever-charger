package com.cyanelix.chargetimer.tesla;

import com.cyanelix.chargetimer.config.TeslaClientConfig;
import com.cyanelix.chargetimer.tesla.domain.ChargeState;
import com.cyanelix.chargetimer.tesla.domain.ChargeStateResponse;
import com.cyanelix.chargetimer.tesla.exception.TeslaClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TeslaClient {
    private final RestTemplate restTemplate;
    private final TeslaClientConfig teslaClientConfig;

    @Autowired
    public TeslaClient(RestTemplate restTemplate, TeslaClientConfig teslaClientConfig) {
        this.restTemplate = restTemplate;
        this.teslaClientConfig = teslaClientConfig;
    }

    public String getAuthToken() {
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

    public Long getIdFromVin() {
        VehiclesResponse vehiclesResponse = restTemplate.getForObject(
                teslaClientConfig.getBaseUrl() + "/api/1/vehicles",
                VehiclesResponse.class);

        if (vehiclesResponse == null) {
            throw new TeslaClientException(
                    "No response received from /api/1/vehicles endpoint on the Tesla API");
        }

        return vehiclesResponse.getVehicles().stream()
                .filter(vehicle -> vehicle.getVin().equals(teslaClientConfig.getVin()))
                .findAny()
                .map(Vehicle::getId)
                .orElseThrow(() -> new TeslaClientException("No vehicle with matching VIN found"));
    }

    public ChargeState getChargeState(Long id) {
        ChargeStateResponse chargeStateResponse = restTemplate.getForObject(
                teslaClientConfig.getBaseUrl() + "/api/1/vehicles/" + id + "/data_request/charge_state",
                ChargeStateResponse.class);

        if (chargeStateResponse == null) {
            throw new TeslaClientException(
                    "No response received from charge_state endpoint on the Tesla API");
        }

        return chargeStateResponse.getChargeState();
    }
}
