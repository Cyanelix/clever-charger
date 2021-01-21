package com.cyanelix.chargetimer.octopus;

import com.cyanelix.chargetimer.octopus.model.UnitRate;
import com.cyanelix.chargetimer.octopus.model.UnitRatesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FutureRatesTest {
    private final Clock clock = Clock.fixed(Instant.parse("2021-01-01T12:15:00Z"), ZoneId.of("UTC"));

    @Mock
    private OctopusClient octopusClient;

    private FutureRates futureRates;

    @BeforeEach
    void beforeEach() {
        futureRates = new FutureRates(octopusClient, clock);
    }

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

    @Test
    void unitRatesFromPastToFuture_get_returnsOnlyCurrentOrFutureRates() {
        // Given...
        List<UnitRate> unitRates = Arrays.asList(
                new UnitRate(BigDecimal.ONE, ZonedDateTime.parse("2021-01-01T11:30Z"),
                        ZonedDateTime.parse("2021-01-01T12:00Z")),
                new UnitRate(BigDecimal.valueOf(2), ZonedDateTime.parse("2021-01-01T12:00Z"),
                        ZonedDateTime.parse("2021-01-01T12:30Z")),
                new UnitRate(BigDecimal.valueOf(3), ZonedDateTime.parse("2021-01-01T12:30Z"),
                        ZonedDateTime.parse("2021-01-01T13:00Z")));

        UnitRatesResponse unitRatesResponse = mock(UnitRatesResponse.class);
        given(unitRatesResponse.getResults()).willReturn(unitRates);

        given(octopusClient.getRatesFromNow()).willReturn(unitRatesResponse);

        // When...
        List<UnitRate> unitRatesFromNow = futureRates.getUnitRatesFromNow();

        // Then...
        assertThat(unitRatesFromNow).extracting("valueIncVat")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.valueOf(3));
    }

    @Test
    void unitRatesOnlyInPast_get_returnEmptyList() {
        // Given...
        List<UnitRate> unitRates = Collections.singletonList(
                new UnitRate(BigDecimal.ONE, ZonedDateTime.parse("2021-01-01T11:30Z"),
                        ZonedDateTime.parse("2021-01-01T12:00Z")));

        UnitRatesResponse unitRatesResponse = mock(UnitRatesResponse.class);
        given(unitRatesResponse.getResults()).willReturn(unitRates);

        given(octopusClient.getRatesFromNow()).willReturn(unitRatesResponse);

        // When...
        List<UnitRate> unitRatesFromNow = futureRates.getUnitRatesFromNow();

        // Then...
        assertThat(unitRatesFromNow).isEmpty();
    }
}