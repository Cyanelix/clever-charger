package com.cyanelix.chargetimer.tesla;

import com.cyanelix.chargetimer.config.TeslaClientConfig;
import com.cyanelix.chargetimer.tesla.model.ChargeState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.verify.VerificationTimes;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8787})
class TeslaClientTest {
    private final static String AUTH_TOKEN = "auth-token";

    private TeslaClientConfig teslaClientConfig;
    private TeslaClient teslaClient;
    private final TeslaApiCache teslaApiCache = new TeslaApiCache();

    @BeforeEach
    public void setup(MockServerClient mockServerClient) {
        mockServerClient.reset();

        teslaClientConfig = mock(TeslaClientConfig.class);
        given(teslaClientConfig.getBaseUrl()).willReturn("http://localhost:8787");

        teslaClient = new TeslaClient(new RestTemplate(), teslaClientConfig, teslaApiCache);
    }

    @Test
    void authAndIdCached_getChargeState_returnsValue(MockServerClient mockServerClient) {
        // Given...
        Long id = 123L;
        int expectedBatteryLevel = 100;

        teslaApiCache.setAuthToken(AUTH_TOKEN);
        teslaApiCache.setId(id);

        mockChargeStateEndpoint(mockServerClient, id, expectedBatteryLevel);

        // When...
        ChargeState chargeState = teslaClient.getChargeState();

        // Then...
        assertThat(chargeState.getBatteryLevel()).isEqualTo(expectedBatteryLevel);

        mockServerClient.verify(
                request().withPath("/oauth/token"), VerificationTimes.exactly(0));
        mockServerClient.verify(
                request().withPath("/api/1/vehicles"), VerificationTimes.exactly(0));
    }

    @Test
    void onlyIdCached_getChargeState_returnsValueWithAuthRequest(MockServerClient mockServerClient) {
        // Given...
        Long id = 123L;
        int expectedBatteryLevel = 100;

        teslaApiCache.setId(id);

        mockAuthTokenEndpoint(mockServerClient);
        mockChargeStateEndpoint(mockServerClient, id, expectedBatteryLevel);

        // When...
        ChargeState chargeState = teslaClient.getChargeState();

        // Then...
        assertThat(chargeState.getBatteryLevel()).isEqualTo(expectedBatteryLevel);

        mockServerClient.verify(
                request().withPath("/oauth/token"), VerificationTimes.once());
        mockServerClient.verify(
                request().withPath("/api/1/vehicles"), VerificationTimes.exactly(0));
    }

    @Test
    void onlyAuthCached_getChargeState_returnsValueWithIdRequest(MockServerClient mockServerClient) {
        // Given...
        Long id = 123L;
        int expectedBatteryLevel = 100;

        String vin = "DUMMY-VIN";
        given(teslaClientConfig.getVin()).willReturn(vin);

        teslaApiCache.setAuthToken(AUTH_TOKEN);

        mockVehiclesEndpoint(mockServerClient, id, vin);
        mockChargeStateEndpoint(mockServerClient, id, expectedBatteryLevel);

        // When...
        ChargeState chargeState = teslaClient.getChargeState();

        // Then...
        assertThat(chargeState.getBatteryLevel()).isEqualTo(expectedBatteryLevel);

        mockServerClient.verify(
                request().withPath("/oauth/token"), VerificationTimes.exactly(0));
        mockServerClient.verify(
                request().withPath("/api/1/vehicles"), VerificationTimes.once());
    }

    @Test
    void nothingCached_getChargeState_returnsValueWithAuthAndIdRequests(MockServerClient mockServerClient) {
        // Given...
        Long id = 123L;
        int expectedBatteryLevel = 100;

        String vin = "DUMMY-VIN";
        given(teslaClientConfig.getVin()).willReturn(vin);

        mockAuthTokenEndpoint(mockServerClient);
        mockVehiclesEndpoint(mockServerClient, id, vin);
        mockChargeStateEndpoint(mockServerClient, id, expectedBatteryLevel);

        // When...
        ChargeState chargeState = teslaClient.getChargeState();

        // Then...
        assertThat(chargeState.getBatteryLevel()).isEqualTo(expectedBatteryLevel);

        mockServerClient.verify(
                request().withPath("/oauth/token"), VerificationTimes.once());
        mockServerClient.verify(
                request().withPath("/api/1/vehicles"), VerificationTimes.once());
    }

    private void mockAuthTokenEndpoint(MockServerClient mockServerClient) {
        given(teslaClientConfig.getClientId()).willReturn("client-id");
        given(teslaClientConfig.getClientSecret()).willReturn("client-secret");
        given(teslaClientConfig.getEmail()).willReturn("email-address");
        given(teslaClientConfig.getPassword()).willReturn("dummy-password");

        mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withPath("/oauth/token")
                        .withBody(
                                json("{\n" +
                                                "  \"grant_type\": \"password\",\n" +
                                                "  \"client_id\": \"client-id\",\n" +
                                                "  \"client_secret\": \"client-secret\",\n" +
                                                "  \"email\": \"email-address\",\n" +
                                                "  \"password\": \"dummy-password\"\n" +
                                                "}",
                                        MatchType.STRICT)
                        )
        ).respond(response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody("{\n" +
                        "    \"access_token\": \"" + AUTH_TOKEN + "\",\n" +
                        "    \"token_type\": \"bearer\",\n" +
                        "    \"expires_in\": 3888000,\n" +
                        "    \"refresh_token\": \"refresh-token\",\n" +
                        "    \"created_at\": 1593297025\n" +
                        "}")
        );
    }

    private void mockVehiclesEndpoint(MockServerClient mockServerClient, Long expectedId, String vin) {
        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withHeader("Authorization", "Bearer auth-token")
                        .withPath("/api/1/vehicles")
        ).respond(response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody("{\n" +
                        "    \"response\": [\n" +
                        "        {\n" +
                        "            \"id\": " + expectedId + ",\n" +
                        "            \"vehicle_id\": 456,\n" +
                        "            \"vin\": \"" + vin + "\",\n" +
                        "            \"display_name\": \"Display Name\",\n" +
                        "            \"option_codes\": \"AD15,MDL3\",\n" +
                        "            \"color\": null,\n" +
                        "            \"tokens\": [],\n" +
                        "            \"state\": \"online\",\n" +
                        "            \"in_service\": false,\n" +
                        "            \"id_s\": \"999\",\n" +
                        "            \"calendar_enabled\": true,\n" +
                        "            \"api_version\": 8,\n" +
                        "            \"backseat_token\": null,\n" +
                        "            \"backseat_token_updated_at\": null\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"count\": 1\n" +
                        "}"));
    }

    private void mockChargeStateEndpoint(MockServerClient mockServerClient, Long id, int expectedBatteryLevel) {
        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withHeader("Authorization", "Bearer " + AUTH_TOKEN)
                        .withPath("/api/1/vehicles/" + id + "/data_request/charge_state")
        ).respond(response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody("{\n" +
                        "    \"response\": {\n" +
                        "        \"battery_heater_on\": false,\n" +
                        "        \"battery_level\": " + expectedBatteryLevel + ",\n" +
                        "        \"battery_range\": 260.82,\n" +
                        "        \"charge_current_request\": 32,\n" +
                        "        \"charge_current_request_max\": 32,\n" +
                        "        \"charge_enable_request\": false,\n" +
                        "        \"charge_energy_added\": 0.0,\n" +
                        "        \"charge_limit_soc\": 90,\n" +
                        "        \"charge_limit_soc_max\": 100,\n" +
                        "        \"charge_limit_soc_min\": 50,\n" +
                        "        \"charge_limit_soc_std\": 90,\n" +
                        "        \"charge_miles_added_ideal\": 0.0,\n" +
                        "        \"charge_miles_added_rated\": 0.0,\n" +
                        "        \"charge_port_cold_weather_mode\": false,\n" +
                        "        \"charge_port_door_open\": true,\n" +
                        "        \"charge_port_latch\": \"Engaged\",\n" +
                        "        \"charge_rate\": 0.0,\n" +
                        "        \"charge_to_max_range\": false,\n" +
                        "        \"charger_actual_current\": 0,\n" +
                        "        \"charger_phases\": 1,\n" +
                        "        \"charger_pilot_current\": 32,\n" +
                        "        \"charger_power\": 0,\n" +
                        "        \"charger_voltage\": 2,\n" +
                        "        \"charging_state\": \"Stopped\",\n" +
                        "        \"conn_charge_cable\": \"IEC\",\n" +
                        "        \"est_battery_range\": 204.99,\n" +
                        "        \"fast_charger_brand\": \"<invalid>\",\n" +
                        "        \"fast_charger_present\": false,\n" +
                        "        \"fast_charger_type\": \"ACSingleWireCAN\",\n" +
                        "        \"ideal_battery_range\": 260.82,\n" +
                        "        \"managed_charging_active\": false,\n" +
                        "        \"managed_charging_start_time\": null,\n" +
                        "        \"managed_charging_user_canceled\": false,\n" +
                        "        \"max_range_charge_counter\": 0,\n" +
                        "        \"minutes_to_full_charge\": 0,\n" +
                        "        \"not_enough_power_to_heat\": null,\n" +
                        "        \"scheduled_charging_pending\": true,\n" +
                        "        \"scheduled_charging_start_time\": 1593475200,\n" +
                        "        \"time_to_full_charge\": 0.0,\n" +
                        "        \"timestamp\": 1593440340659,\n" +
                        "        \"trip_charging\": false,\n" +
                        "        \"usable_battery_level\": 85,\n" +
                        "        \"user_charge_enable_request\": null\n" +
                        "    }\n" +
                        "}"));
    }
}