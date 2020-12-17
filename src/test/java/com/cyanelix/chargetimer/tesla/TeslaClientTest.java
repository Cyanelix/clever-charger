package com.cyanelix.chargetimer.tesla;

import com.cyanelix.chargetimer.config.TeslaClientConfig;
import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.tesla.model.ChargeState;
import com.cyanelix.chargetimer.testutil.MockRequest;
import com.cyanelix.chargetimer.testutil.MockServerUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.verify.VerificationTimes;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.Parameter.param;
import static org.mockserver.model.ParameterBody.params;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8787})
class TeslaClientTest {
    private TeslaClientConfig teslaClientConfig;
    private TeslaClient teslaClient;
    private final TeslaApiCache teslaApiCache = new TeslaApiCache();

    @BeforeEach
    public void setup(MockServerClient mockServerClient) {
        mockServerClient.reset();

        teslaClientConfig = mock(TeslaClientConfig.class);
        given(teslaClientConfig.getBaseUrl()).willReturn("http://localhost:8787");
        given(teslaClientConfig.getWakeUpWaitSeconds()).willReturn(2);

        teslaClient = new TeslaClient(new RestTemplate(), teslaClientConfig, teslaApiCache);
    }

    @Test
    void authAndIdCached_carAsleep_getChargeState_throwsException(MockServerClient mockServerClient) {
        // Given...
        Long id = 123L;

        teslaApiCache.setAuthToken(MockRequest.AUTH_TOKEN);
        teslaApiCache.setId(id);

        MockServerUtil.mockChargeStateEndpointAsleep(mockServerClient, id);

        // When...
        Throwable throwable = catchThrowable(() -> teslaClient.getChargeState());

        // Then...
        assertThat(throwable).isInstanceOf(HttpClientErrorException.class);
        assertThat(((HttpClientErrorException) throwable).getStatusCode())
                .isEqualTo(HttpStatus.REQUEST_TIMEOUT);
    }

    @Test
    void authAndIdCached_getChargeState_returnsValue(MockServerClient mockServerClient) {
        // Given...
        Long id = 123L;
        int expectedBatteryLevel = 100;

        teslaApiCache.setAuthToken(MockRequest.AUTH_TOKEN);
        teslaApiCache.setId(id);

        MockServerUtil.mockChargeStateEndpoint(mockServerClient, id, expectedBatteryLevel, "Stopped");

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
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, id, expectedBatteryLevel, "Stopped");

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

        teslaApiCache.setAuthToken(MockRequest.AUTH_TOKEN);

        MockServerUtil.mockVehiclesEndpoint(mockServerClient, id, vin);
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, id, expectedBatteryLevel, "Stopped");

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
        MockServerUtil.mockVehiclesEndpoint(mockServerClient, id, vin);
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, id, expectedBatteryLevel, "Stopped");

        // When...
        ChargeState chargeState = teslaClient.getChargeState();

        // Then...
        assertThat(chargeState.getBatteryLevel()).isEqualTo(expectedBatteryLevel);

        mockServerClient.verify(
                request().withPath("/oauth/token"), VerificationTimes.once());
        mockServerClient.verify(
                request().withPath("/api/1/vehicles"), VerificationTimes.once());
    }

    @Test
    void authAndIdCached_startCharging_success(MockServerClient mockServerClient) {
        // Given...
        Long id = 123L;

        teslaApiCache.setAuthToken(MockRequest.AUTH_TOKEN);
        teslaApiCache.setId(id);

        mockStartChargingEndpoint_success(mockServerClient, id);

        // When...
        teslaClient.startCharging();

        // Then...
        mockServerClient.verify(
                request().withPath("/oauth/token"), VerificationTimes.exactly(0));
        mockServerClient.verify(
                request().withPath("/api/1/vehicles"), VerificationTimes.exactly(0));
        mockServerClient.verify(
                request().withPath("/api/1/vehicles/" + id + "/command/charge_start"), VerificationTimes.once());
    }

    @Test
    void authAndIdCached_stopCharging_success(MockServerClient mockServerClient) {
        // Given...
        Long id = 123L;

        teslaApiCache.setAuthToken(MockRequest.AUTH_TOKEN);
        teslaApiCache.setId(id);

        mockStopChargingEndpoint_success(mockServerClient, id);

        // When...
        teslaClient.stopCharging();

        // Then...
        mockServerClient.verify(
                request().withPath("/oauth/token"), VerificationTimes.exactly(0));
        mockServerClient.verify(
                request().withPath("/api/1/vehicles"), VerificationTimes.exactly(0));
        mockServerClient.verify(
                request().withPath("/api/1/vehicles/" + id + "/command/charge_stop"), VerificationTimes.once());
    }

    @Test
    void authAndIdCached_sleeping_setChargeLimit_wakesSuccessfully(MockServerClient mockServerClient) {
        // Given...
        Long id = 123L;

        teslaApiCache.setAuthToken(MockRequest.AUTH_TOKEN);
        teslaApiCache.setId(id);

        // The car is initially asleep
        MockServerUtil.mockSetChargeLimitEndpointAsleep(mockServerClient, id);
        MockServerUtil.mockWakeEndpoint(mockServerClient, id, "asleep");

        // But then wakes up
        MockServerUtil.mockWakeEndpoint(mockServerClient, id, "online");
        MockServerUtil.mockSetChargeLimitEndpoint(mockServerClient, id);

        // When...
        teslaClient.setChargeLimit(ChargeLevel.of(90));

        // Then...
        mockServerClient.verify(
                request()
                        .withPath("/api/1/vehicles/" + id + "/command/set_charge_limit")
                        .withBody(params(param("percent", "90"))),
                request().withPath("/api/1/vehicles/" + id + "/wake_up"),
                request().withPath("/api/1/vehicles/" + id + "/wake_up"),
                request()
                        .withPath("/api/1/vehicles/" + id + "/command/set_charge_limit")
                        .withBody(params(param("percent", "90"))));
    }

    @Test
    void authAndIdCached_setChargeLimit_success(MockServerClient mockServerClient) {
        // Given...
        Long id = 123L;

        teslaApiCache.setAuthToken(MockRequest.AUTH_TOKEN);
        teslaApiCache.setId(id);

        mockSetChargeLimitEndpoint_success(mockServerClient, 90, id);

        // When...
        teslaClient.setChargeLimit(ChargeLevel.of(90));

        // Then...
        mockServerClient.verify(
                request().withPath("/oauth/token"), VerificationTimes.exactly(0));
        mockServerClient.verify(
                request().withPath("/api/1/vehicles"), VerificationTimes.exactly(0));
        mockServerClient.verify(
                request().withPath("/api/1/vehicles/" + id + "/command/set_charge_limit"));
        mockServerClient.verify(
                request().withBody(params(param("percent", "90"))));
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
                        "    \"access_token\": \"" + MockRequest.AUTH_TOKEN + "\",\n" +
                        "    \"token_type\": \"bearer\",\n" +
                        "    \"expires_in\": 3888000,\n" +
                        "    \"refresh_token\": \"refresh-token\",\n" +
                        "    \"created_at\": 1593297025\n" +
                        "}")
        );
    }

    private void mockStartChargingEndpoint_success(MockServerClient mockServerClient, Long id) {
        mockEmptyCommandSuccess(mockServerClient, id, "charge_start");
    }

    private void mockStopChargingEndpoint_success(MockServerClient mockServerClient, Long id) {
        mockEmptyCommandSuccess(mockServerClient, id, "charge_stop");
    }

    private void mockEmptyCommandSuccess(MockServerClient mockServerClient, Long id, String command) {
        mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withHeader("Authorization", "Bearer " + MockRequest.AUTH_TOKEN)
                        .withPath("/api/1/vehicles/" + id + "/command/" + command)
        ).respond(successResponse());
    }

    private void mockSetChargeLimitEndpoint_success(MockServerClient mockServerClient,
                                                    int expectedPercentage, Long id) {
        mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withBody(params(param("percent", Integer.toString(expectedPercentage))))
                        .withPath("/api/1/vehicles/" + id + "/command/set_charge_limit")
        ).respond(successResponse());
    }

    private HttpResponse successResponse() {
        return response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withBody("{\n" +
                        "    \"response\": {\n" +
                        "        \"reason\": \"\",\n" +
                        "        \"result\": true\n" +
                        "    }" +
                        "}");
    }
}