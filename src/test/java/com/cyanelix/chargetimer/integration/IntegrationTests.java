package com.cyanelix.chargetimer.integration;

import com.cyanelix.chargetimer.charges.ChargeScheduler;
import com.cyanelix.chargetimer.charges.ChargeStateService;
import com.cyanelix.chargetimer.charges.RequiredChargesRepository;
import com.cyanelix.chargetimer.config.TeslaClientConfig;
import com.cyanelix.chargetimer.electricity.ChargeCalculator;
import com.cyanelix.chargetimer.electricity.FlexibleOctopusTariff;
import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import com.cyanelix.chargetimer.microtypes.WeeklyTime;
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

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8787})
public class IntegrationTests {
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final Long VEHICLE_ID = 1L;
    private static final String VIN = "ABC123";

    private static final ZonedDateTime SIX_AM = ZonedDateTime.parse("2020-01-01T06:00:00Z");
    private static final ZonedDateTime SIX_AM_TOMORROW = ZonedDateTime.parse("2020-01-02T06:00:00Z");

    // Start tests at 0100 on 01/01/2020 (a Wednesday)
    private final MutableClock clock = new MutableClock(
            Clock.fixed(Instant.parse("2020-01-01T01:00:00Z"), UTC));

    private final RequiredChargesRepository requiredChargesRepository = new RequiredChargesRepository(clock);

    private final ChargeCalculator chargeCalculator = new ChargeCalculator(new FlexibleOctopusTariff(), clock);

    private ChargeScheduler chargeScheduler;

    @BeforeEach
    public void setup(MockServerClient mockServerClient) {
        mockServerClient.reset();

        TeslaClientConfig teslaClientConfig = new TeslaClientConfig();
        teslaClientConfig.setBaseUrl("http://localhost:8787");
        teslaClientConfig.setVin(VIN);

        TeslaApiCache teslaApiCache = new TeslaApiCache();
        teslaApiCache.setAuthToken(MockRequest.AUTH_TOKEN);
        teslaApiCache.setId(VEHICLE_ID);

        TeslaClient teslaClient = new TeslaClient(new RestTemplate(), teslaClientConfig, teslaApiCache);

        ChargeStateService chargeStateService = new ChargeStateService(teslaClient);

        chargeScheduler = new ChargeScheduler(teslaClient, chargeStateService, requiredChargesRepository, chargeCalculator, clock);

        MockServerUtil.mockVehiclesEndpoint(mockServerClient, VEHICLE_ID, VIN);
    }

    @Test
    void fullyCharged_doNothing(MockServerClient mockServerClient) {
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 100, "Stopped");

        chargeScheduler.chargeIfNeeded();

        verifyNoChangeToCharging(mockServerClient);
    }

    @Test
    void chargedToNextScheduledAmount_doNothing(MockServerClient mockServerClient) {
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 90, "Stopped");
        requiredChargesRepository.addException(RequiredCharge.of(ChargeLevel.of(90), SIX_AM));

        chargeScheduler.chargeIfNeeded();

        verifyNoChangeToCharging(mockServerClient);
    }

    @Test
    void needToCharge_couldChargeInOnePeriodButCheaperInTwo_chooseCheapestOption(MockServerClient mockServerClient) {
        MockServerUtil.mockSetChargeLimitEndpoint(mockServerClient, VEHICLE_ID);
        MockServerUtil.mockStartChargeEndpoint(mockServerClient, VEHICLE_ID);
        MockServerUtil.mockStopChargeEndpoint(mockServerClient, VEHICLE_ID);

        // Need 90% by 0600 tomorrow.
        requiredChargesRepository.addException(RequiredCharge.of(ChargeLevel.of(90), SIX_AM_TOMORROW));

        // Currently have 10%, will need 2 overnight charging sessions.
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 10, "Stopped");

        // Call chargeIfNeeded at 0100 on day 1; should start charging - currently night rate.
        chargeScheduler.chargeIfNeeded();
        mockServerClient.verify(MockRequest.setChargeLimit(VEHICLE_ID, 90));
        mockServerClient.verify(MockRequest.startCharging(VEHICLE_ID));

        // Call chargeIfNeeded at 0531 on day 1; should stop - we're in the day rate period and nearly at the target SoC.
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 80, "Charging");
        clock.setClock(Clock.fixed(Instant.parse("2020-01-01T05:31:00Z"), UTC));
        chargeScheduler.chargeIfNeeded();
        mockServerClient.verify(MockRequest.stopCharging(VEHICLE_ID));

        // Call chargeIfNeeded at 0031 on day 2; should start charging again
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 80, "Stopped");
        clock.setClock(Clock.fixed(Instant.parse("2020-01-02T00:31:00Z"), UTC));
        chargeScheduler.chargeIfNeeded();
        mockServerClient.verify(MockRequest.setChargeLimit(VEHICLE_ID, 90));
        mockServerClient.verify(MockRequest.startCharging(VEHICLE_ID));
    }

    @Test
    void needToCharge_notEnoughTimeToReachTarget_startCharging(MockServerClient mockServerClient) {
        MockServerUtil.mockSetChargeLimitEndpoint(mockServerClient, VEHICLE_ID);
        MockServerUtil.mockStartChargeEndpoint(mockServerClient, VEHICLE_ID);

        // Currently have 10% battery
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 10, "Stopped");

        // Need 90% in 4.5 hours time (not long enough)
        requiredChargesRepository.addException(RequiredCharge.of(ChargeLevel.of(90), SIX_AM));

        chargeScheduler.chargeIfNeeded();

        mockServerClient.verify(MockRequest.setChargeLimit(VEHICLE_ID, 90));
        mockServerClient.verify(MockRequest.startCharging(VEHICLE_ID));
    }

    @Test
    void scheduledForTomorrowWithExceptionForToday_startCharging(MockServerClient mockServerClient) {
        MockServerUtil.mockSetChargeLimitEndpoint(mockServerClient, VEHICLE_ID);
        MockServerUtil.mockStartChargeEndpoint(mockServerClient, VEHICLE_ID);

        // Schedule a charge for Thursday (tomorrow)
        requiredChargesRepository.addWeekly(
                new WeeklyTime(DayOfWeek.THURSDAY, LocalTime.NOON), ChargeLevel.of(70));

        // Add an exception for today
        requiredChargesRepository.addException(RequiredCharge.of(ChargeLevel.of(90), SIX_AM));

        // Currently have 80% (i.e. don't need to charge for schedule, but do need to for the exception)
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 80, "Stopped");

        chargeScheduler.chargeIfNeeded();

        mockServerClient.verify(MockRequest.setChargeLimit(VEHICLE_ID, 90));
        mockServerClient.verify(MockRequest.startCharging(VEHICLE_ID));
    }

    @Test
    void scheduledForTomorrowWithNoException_startCharging(MockServerClient mockServerClient) {
        MockServerUtil.mockSetChargeLimitEndpoint(mockServerClient, VEHICLE_ID);
        MockServerUtil.mockStartChargeEndpoint(mockServerClient, VEHICLE_ID);

        // Schedule a charge for Thursday (tomorrow)
        requiredChargesRepository.addWeekly(
                new WeeklyTime(DayOfWeek.THURSDAY, LocalTime.NOON), ChargeLevel.of(70));

        // Currently have 65% (i.e. don't need to charge today, but will need to tomorrow)
        MockServerUtil.mockChargeStateEndpoint(mockServerClient, VEHICLE_ID, 65, "Stopped");

        // Run chargeIfNeeded on day 1; expect charging to start
        chargeScheduler.chargeIfNeeded();
        mockServerClient.verify(MockRequest.setChargeLimit(VEHICLE_ID, 70));
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
