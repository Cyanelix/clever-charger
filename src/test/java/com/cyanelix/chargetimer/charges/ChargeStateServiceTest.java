package com.cyanelix.chargetimer.charges;

import com.cyanelix.chargetimer.tesla.TeslaClient;
import com.cyanelix.chargetimer.tesla.model.ChargeState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ChargeStateServiceTest {
    @Mock
    private TeslaClient teslaClient;

    @InjectMocks
    private ChargeStateService chargeStateService;

    @Test
    void getChargeState_success() {
        // Given...
        ChargeState chargeState = new ChargeState();
        given(teslaClient.getChargeState()).willReturn(chargeState);

        // When...
        ChargeState returnedChargeState = chargeStateService.getChargeState();

        // Then...
        assertThat(returnedChargeState).isSameAs(chargeState);
    }

    @Test
    void getChargeState_requestTimeout_returnCachedChargeState() {
        // Given...
        ChargeState chargeState = new ChargeState();
        chargeState.setBatteryLevel(50);
        chargeState.setChargingState("Stopped");

        given(teslaClient.getChargeState())
                .willReturn(chargeState)
                .willThrow(new HttpClientErrorException(HttpStatus.REQUEST_TIMEOUT));

        chargeStateService.getChargeState();

        // When...
        ChargeState returnedChargeState = chargeStateService.getChargeState();

        // Then...
        assertThat(returnedChargeState.getBatteryLevel()).isEqualTo(50);
        assertThat(returnedChargeState.getChargingState()).isEqualTo("Unknown");
    }
}