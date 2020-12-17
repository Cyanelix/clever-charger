package com.cyanelix.chargetimer.testutil;

import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.Times;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpStatusCode;
import org.springframework.http.HttpHeaders;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class MockServerUtil {
    public static void mockVehiclesEndpoint(MockServerClient mockServerClient, Long expectedId, String vin) {
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

    public static void mockChargeStateEndpointAsleep(MockServerClient mockServerClient, Long id) {
        mockServerClient.upsert(
                new Expectation(MockRequest.getChargeState(id))
                        .withId("single-charge-state")
                        .thenRespond(
                                response()
                                        .withStatusCode(HttpStatusCode.REQUEST_TIMEOUT_408.code())
                                        .withHeader("Content-Type", "application/json; charset=utf-8")
                                        .withBody("{\n" +
                                                "    \"response\": null,\n" +
                                                "    \"error\": \"vehicle unavailable: {:error=>\\\"vehicle unavailable:\\\"}\",\n" +
                                                "    \"error_description\": \"\"\n" +
                                                "}")));
    }

    public static void mockChargeStateEndpoint(MockServerClient mockServerClient, Long id, int batteryLevel, String chargingState) {
        mockServerClient.upsert(
                new Expectation(MockRequest.getChargeState(id))
                        .withId("single-charge-state")
                        .thenRespond(
                                response()
                                        .withStatusCode(HttpStatusCode.OK_200.code())
                                        .withHeader("Content-Type", "application/json; charset=utf-8")
                                        .withBody("{\n" +
                                                "    \"response\": {\n" +
                                                "        \"battery_heater_on\": false,\n" +
                                                "        \"battery_level\": " + batteryLevel + ",\n" +
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
                                                "        \"charging_state\": \"" + chargingState + "\",\n" +
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
                                                "}")));
    }

    public static void mockSetChargeLimitEndpoint(MockServerClient mockServerClient, Long id) {
        mockCommandEndpoint(mockServerClient, "/api/1/vehicles/" + id + "/command/set_charge_limit");
    }

    public static void mockSetChargeLimitEndpointAsleep(MockServerClient mockServerClient, Long id) {
        mockCommandEndpointAsleep(mockServerClient, "/api/1/vehicles/" + id + "/command/set_charge_limit");
    }

    public static void mockStartChargeEndpoint(MockServerClient mockServerClient, Long id) {
        mockCommandEndpoint(mockServerClient, "/api/1/vehicles/" + id + "/command/charge_start");
    }

    public static void mockStopChargeEndpoint(MockServerClient mockServerClient, Long id) {
        mockCommandEndpoint(mockServerClient, "/api/1/vehicles/" + id + "/command/charge_stop");
    }

    public static void mockWakeEndpoint(MockServerClient mockServerClient, Long id, String state) {
        mockServerClient.when(MockRequest.wakeUp(id), Times.once())
                .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "    \"response\": {\n" +
                                "        \"id\": " + id + ",\n" +
                                "        \"vehicle_id\": 456,\n" +
                                "        \"vin\": \"ABC123\",\n" +
                                "        \"display_name\": \"Display Name\",\n" +
                                "        \"option_codes\": \"AD15,MDL3\",\n" +
                                "        \"color\": null,\n" +
                                "        \"tokens\": [],\n" +
                                "        \"state\": \"" + state + "\",\n" +
                                "        \"in_service\": false,\n" +
                                "        \"id_s\": \"999\",\n" +
                                "        \"calendar_enabled\": true,\n" +
                                "        \"api_version\": 8,\n" +
                                "        \"backseat_token\": null,\n" +
                                "        \"backseat_token_updated_at\": null\n" +
                                "    }\n" +
                                "}"));
    }

    private static void mockCommandEndpoint(MockServerClient mockServerClient, String endpoint) {
        mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withHeader("Authorization", "Bearer " + MockRequest.AUTH_TOKEN)
                        .withPath(endpoint)
        ).respond(response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .withBody("{\n" +
                        "    \"response\": {\n" +
                        "        \"reason\": \"\",\n" +
                        "        \"result\": true\n" +
                        "    }\n" +
                        "}"));
    }

    private static void mockCommandEndpointAsleep(MockServerClient mockServerClient, String endpoint) {
        mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withHeader("Authorization", "Bearer " + MockRequest.AUTH_TOKEN)
                        .withPath(endpoint),
                Times.once()
        ).respond(response()
                .withStatusCode(HttpStatusCode.REQUEST_TIMEOUT_408.code())
                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .withBody("{\n" +
                        "    \"response\": null,\n" +
                        "    \"error\": \"vehicle unavailable: {:error=>\\\"vehicle unavailable:\\\"}\",\n" +
                        "    \"error_description\": \"\"\n" +
                        "}"));
    }
}
