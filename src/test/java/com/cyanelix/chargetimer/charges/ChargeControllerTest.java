package com.cyanelix.chargetimer.charges;

import com.cyanelix.chargetimer.electricity.ChargeCalculator;
import com.cyanelix.chargetimer.electricity.RatePeriod;
import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.RequiredCharge;
import com.cyanelix.chargetimer.tesla.TeslaClient;
import com.cyanelix.chargetimer.tesla.model.ChargeState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeControllerTest {
    private final Clock clock = Clock.fixed(Instant.parse("2020-01-01T12:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private RatePeriod mockRatePeriod;

    @Mock
    private TeslaClient teslaClient;

    @Mock
    private RequiredChargesRepository requiredChargesRepository;

    @Mock
    private ChargeCalculator chargeCalculator;

    private ChargeController chargeController;

    @BeforeEach
    void setUp() {
        chargeController = new ChargeController(teslaClient, requiredChargesRepository, chargeCalculator, clock);
    }

    @Test
    void chargeStateUnavailable_doNothing() {
        // Given...
        given(teslaClient.getChargeState())
                .willThrow(new HttpClientErrorException(HttpStatus.REQUEST_TIMEOUT));

        // When...
        chargeController.chargeIfNeeded();

        // Then...
        verifyNoInteractions(requiredChargesRepository);
        verifyNoInteractions(chargeCalculator);
        verifyNoMoreInteractions(teslaClient);
    }

    @Test
    void notPluggedIn_doNothing() {
        // Given...
        ChargeState chargeState = new ChargeState();
        chargeState.setChargingState("Disconnected");

        given(teslaClient.getChargeState()).willReturn(chargeState);

        // When...
        chargeController.chargeIfNeeded();

        // Then...
        verifyNoInteractions(requiredChargesRepository);
        verifyNoInteractions(chargeCalculator);
        verifyNoMoreInteractions(teslaClient);
    }

    @Test
    void noScheduledCharges_doNothing() {
        // Given...
        ChargeState chargeState = new ChargeState();
        chargeState.setChargingState("Stopped");

        given(teslaClient.getChargeState()).willReturn(chargeState);
        given(requiredChargesRepository.getNextRequiredCharge()).willReturn(null);

        // When...
        chargeController.chargeIfNeeded();

        // Then...
        verifyNoInteractions(chargeCalculator);
        verifyNoMoreInteractions(teslaClient);
    }

    @Test
    void nextChargeStartInPastNotReadyToCharge_doNothing() {
        // Given...
        ChargeState chargeState = new ChargeState();
        chargeState.setChargingState("Charging");
        given(teslaClient.getChargeState()).willReturn(chargeState);

        RequiredCharge requiredCharge = RequiredCharge.of(
                ChargeLevel.of(100), ZonedDateTime.parse("2020-01-01T11:00Z"));
        given(requiredChargesRepository.getNextRequiredCharge()).willReturn(requiredCharge);

        given(mockRatePeriod.chargeNow(clock)).willReturn(true);
        given(chargeCalculator.getNextChargePeriod(any(RequiredCharge.class), any(ChargeLevel.class)))
                .willReturn(mockRatePeriod);

        // When...
        chargeController.chargeIfNeeded();

        // Then...
        verifyNoMoreInteractions(teslaClient);
    }

    @Test
    void nextChargeStartInPastReadyToCharge_startCharging() {
        // Given...
        ChargeState chargeState = new ChargeState();
        chargeState.setChargingState("Stopped");
        given(teslaClient.getChargeState()).willReturn(chargeState);

        RequiredCharge requiredCharge = RequiredCharge.of(
                ChargeLevel.of(100), ZonedDateTime.parse("2020-01-01T11:00Z"));
        given(requiredChargesRepository.getNextRequiredCharge()).willReturn(requiredCharge);

        given(mockRatePeriod.chargeNow(clock)).willReturn(true);
        given(chargeCalculator.getNextChargePeriod(any(RequiredCharge.class), any(ChargeLevel.class)))
                .willReturn(mockRatePeriod);

        // When...
        chargeController.chargeIfNeeded();

        // Then...
        verify(teslaClient).startCharging();
    }

    @Test
    void nextChargeStartInFutureNotCharging_doNothing() {
        ChargeState chargeState = new ChargeState();
        chargeState.setChargingState("Stopped");
        given(teslaClient.getChargeState()).willReturn(chargeState);

        RequiredCharge requiredCharge = RequiredCharge.of(
                ChargeLevel.of(100), ZonedDateTime.parse("2020-01-01T13:00Z"));
        given(requiredChargesRepository.getNextRequiredCharge()).willReturn(requiredCharge);

        given(mockRatePeriod.chargeNow(clock)).willReturn(false);
        given(chargeCalculator.getNextChargePeriod(any(RequiredCharge.class), any(ChargeLevel.class)))
                .willReturn(mockRatePeriod);

        // When...
        chargeController.chargeIfNeeded();

        // Then...
        verifyNoMoreInteractions(teslaClient);
    }

    @Test
    void nextChargeStartInFutureCharging_stopCharging() {
        ChargeState chargeState = new ChargeState();
        chargeState.setChargingState("Charging");
        given(teslaClient.getChargeState()).willReturn(chargeState);

        RequiredCharge requiredCharge = RequiredCharge.of(
                ChargeLevel.of(100), ZonedDateTime.parse("2020-01-01T13:00Z"));
        given(requiredChargesRepository.getNextRequiredCharge()).willReturn(requiredCharge);

        given(mockRatePeriod.chargeNow(clock)).willReturn(false);
        given(chargeCalculator.getNextChargePeriod(any(RequiredCharge.class), any(ChargeLevel.class)))
                .willReturn(mockRatePeriod);

        // When...
        chargeController.chargeIfNeeded();

        // Then...
        verify(teslaClient).stopCharging();
    }

    @Test
    void nextChargePeriodIsNullNotCharging_doNothing() {
        // Given...
        ChargeState chargeState = new ChargeState();
        chargeState.setChargingState("Stopped");
        given(teslaClient.getChargeState()).willReturn(chargeState);

        RequiredCharge requiredCharge = RequiredCharge.of(
                ChargeLevel.of(100), ZonedDateTime.parse("2020-01-01T13:00Z"));
        given(requiredChargesRepository.getNextRequiredCharge()).willReturn(requiredCharge);

        given(chargeCalculator.getNextChargePeriod(any(RequiredCharge.class), any(ChargeLevel.class)))
                .willReturn(RatePeriod.NULL_RATE_PERIOD);

        // When...
        chargeController.chargeIfNeeded();

        // Then...
        verifyNoMoreInteractions(teslaClient);
    }

    @Test
    void nextChargePeriodIsNullIsCharging_doNothing() {
        // Given...
        ChargeState chargeState = new ChargeState();
        chargeState.setChargingState("Charging");
        given(teslaClient.getChargeState()).willReturn(chargeState);

        RequiredCharge requiredCharge = RequiredCharge.of(
                ChargeLevel.of(100), ZonedDateTime.parse("2020-01-01T13:00Z"));
        given(requiredChargesRepository.getNextRequiredCharge()).willReturn(requiredCharge);

        given(chargeCalculator.getNextChargePeriod(any(RequiredCharge.class), any(ChargeLevel.class)))
                .willReturn(RatePeriod.NULL_RATE_PERIOD);

        // When...
        chargeController.chargeIfNeeded();

        // Then...
        verify(teslaClient).stopCharging();
    }
}