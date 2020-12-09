package com.cyanelix.chargetimer.integration;

import com.cyanelix.chargetimer.charges.ChargeController;
import com.cyanelix.chargetimer.charges.RequiredChargesRepository;
import com.cyanelix.chargetimer.config.TeslaClientConfig;
import com.cyanelix.chargetimer.electricity.ChargeCalculator;
import com.cyanelix.chargetimer.electricity.FlexibleOctopusTariff;
import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import com.cyanelix.chargetimer.tesla.TeslaApiCache;
import com.cyanelix.chargetimer.tesla.TeslaClient;
import com.cyanelix.chargetimer.testutil.MockRequest;
import com.cyanelix.chargetimer.testutil.MockServerUtil;
import com.cyanelix.chargetimer.util.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.verify.VerificationTimes;
import org.springframework.web.client.RestTemplate;

import java.time.*;

import static com.cyanelix.chargetimer.testutil.MockServerUtil.AUTH_TOKEN;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8787})
public class IntegrationTests {
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final Long VEHICLE_ID = 1L;
    private static final String VIN = "ABC123";

    private static final ZonedDateTime SIX_AM = ZonedDateTime.parse("2020-01-01T06:00:00Z");
    private static final ZonedDateTime SIX_AM_TOMORROW = ZonedDateTime.parse("2020-01-02T06:00:00Z");

    private final MutableClock clock = new MutableClock(
            Clock.fixed(Instant.parse("2020-01-01T01:00:00Z"), UTC));

    private final RequiredChargesRepository requiredChargesRepository = new RequiredChargesRepository(clock);

    private final ChargeCalculator chargeCalculator = new ChargeCalculator(new FlexibleOctopusTariff(), clock);

    private ChargeController chargeController;

    @BeforeEach
    public void setup(MockServerClient mockServerClient) {
        mockServerClient.reset();

        TeslaClientConfig teslaClientConfig = new TeslaClientConfig();
        teslaClientConfig.setBaseUrl("http://localhost:8787");
        teslaClientConfig.setVin(VIN);

        TeslaApiCache teslaApiCache = new TeslaApiCache();
        teslaApiCache.setAuthToken(AUTH_TOKEN);
        teslaApiCache.setId(VEHICLE_ID);

        TeslaClient teslaClient = new TeslaClient(new RestTemplate(), teslaClientConfig, teslaApiCache);

        chargeController = new ChargeController(teslaClient, requiredChargesRepository, chargeCalculator, clock);

        MockServerUtil.mockVehiclesEndpoint(mockServerClient, VEHICLE_ID, VIN);
    }

    @Test
    void fullyCharged_doNothing(MockServerClient mockServerClient) {
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 100, "Stopped");

        chargeController.chargeIfNeeded();

        verifyNoChangeToCharging(mockServerClient);
    }

    @Test
    void chargedToNextScheduledAmount_doNothing(MockServerClient mockServerClient) {
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 90, "Stopped");
        requiredChargesRepository.addException(RequiredCharge.of(ChargeLevel.of(90), SIX_AM));

        chargeController.chargeIfNeeded();

        verifyNoChangeToCharging(mockServerClient);
    }

    @Test
    void needToCharge_couldChargeInOnePeriodButCheaperInTwo_chooseCheapestOption(MockServerClient mockServerClient) {
        MockServerUtil.mockVehiclesEndpoint(mockServerClient, VEHICLE_ID, VIN);
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 10, "Stopped");
        MockServerUtil.mockSetChargeLimitEndpoint(mockServerClient, VEHICLE_ID);
        MockServerUtil.mockStartChargeEndpoint(mockServerClient, VEHICLE_ID);
        MockServerUtil.mockStopChargeEndpoint(mockServerClient, VEHICLE_ID);

        requiredChargesRepository.addException(RequiredCharge.of(ChargeLevel.of(90), SIX_AM_TOMORROW));

        // TODO: Do I need to assert that these verifies happen in order? And/or should I reset the mockServerClient after each change in time?

        // Call chargeIfNeeded at 0100 on day 1; should start charging - currently night rate.
        chargeController.chargeIfNeeded();
        mockServerClient.verify(MockRequest.setChargeLimit(VEHICLE_ID, 90));
        mockServerClient.verify(MockRequest.startCharging(VEHICLE_ID));

        // Call chargeIfNeeded at 0531 on day 1; should stop - we're in the day rate period and nearly at the target SoC.
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 80, "Charging");
        clock.setClock(Clock.fixed(Instant.parse("2020-01-01T05:31:00Z"), UTC));
        chargeController.chargeIfNeeded();
        mockServerClient.verify(MockRequest.stopCharging(VEHICLE_ID));

        // Call chargeIfNeeded at 0031 on day 2; should start charging again
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 80, "Stopped");
        clock.setClock(Clock.fixed(Instant.parse("2020-01-02T00:31:00Z"), UTC));
        chargeController.chargeIfNeeded();
        mockServerClient.verify(MockRequest.setChargeLimit(VEHICLE_ID, 90));
        mockServerClient.verify(MockRequest.startCharging(VEHICLE_ID));
    }

    private void verifyNoChangeToCharging(MockServerClient mockServerClient) {
        mockServerClient.verify(
                MockRequest.startCharging(VEHICLE_ID), VerificationTimes.exactly(0));
        mockServerClient.verify(
                MockRequest.stopCharging(VEHICLE_ID), VerificationTimes.exactly(0));
        mockServerClient.verify(
                MockRequest.setAnyChargeLimit(VEHICLE_ID), VerificationTimes.exactly(0));
    }
}
