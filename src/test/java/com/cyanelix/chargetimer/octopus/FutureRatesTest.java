package com.cyanelix.chargetimer.octopus;

import com.cyanelix.chargetimer.octopus.model.UnitRate;
import com.cyanelix.chargetimer.octopus.model.UnitRatesResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FutureRatesTest {
    @Mock
    private OctopusClient octopusClient;

    @InjectMocks
    private FutureRates futureRates;

    @Test
    void nullUnitRates_get_callsCacheAgain() {
        // Given...
        List<UnitRate> unitRates = Collections.singletonList(
                new UnitRate(BigDecimal.ONE, ZonedDateTime.parse("2021-01-01T12:00Z"),
                        ZonedDateTime.parse("2021-01-01T12:30Z")));

        UnitRatesResponse unitRatesResponse = mock(UnitRatesResponse.class);
        given(unitRatesResponse.getResults()).willReturn(unitRates);

        given(octopusClient.getRatesFromNow()).willReturn(unitRatesResponse);

        // When...
        List<UnitRate> unitRatesFromNow = futureRates.getUnitRatesFromNow();

        // Then...
        assertThat(unitRatesFromNow).isNotEmpty();
    }
}