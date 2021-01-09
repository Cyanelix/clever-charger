package com.cyanelix.chargetimer.octopus;

import com.cyanelix.chargetimer.config.OctopusClientConfig;
import com.cyanelix.chargetimer.octopus.model.UnitRate;
import com.cyanelix.chargetimer.octopus.model.UnitRatesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {8787})
class OctopusClientTest {
    private final OctopusClientConfig octopusClientConfig = new OctopusClientConfig();
    private final Clock clock = Clock.fixed(Instant.parse("2020-01-01T12:00:00Z"), ZoneId.of("UTC"));

    private final OctopusClient octopusClient = new OctopusClient(new RestTemplate(), octopusClientConfig, clock);

    @BeforeEach
    public void setup(MockServerClient mockServerClient) {
        mockServerClient.reset();

        octopusClientConfig.setBaseUrl("http://localhost:8787");
    }

    @Test
    void getRatesFromNow_returnsExpectedRates(MockServerClient mockServerClient) {
        // Given...
        mockServerClient.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/v1/products/AGILE-18-02-21/electricity-tariffs/E-1R-AGILE-18-02-21-C/standard-unit-rates/")
                        .withQueryStringParameter("period_from", "2020-01-01T12:00:00Z%5BUTC%5D")
        ).respond(HttpResponse.response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                        "    \"count\": 2,\n" +
                        "    \"next\": null,\n" +
                        "    \"previous\": null,\n" +
                        "    \"results\": [\n" +
                        "        {\n" +
                        "            \"value_exc_vat\": 8.8,\n" +
                        "            \"value_inc_vat\": 9.24,\n" +
                        "            \"valid_from\": \"2020-01-01T12:30:00Z\",\n" +
                        "            \"valid_to\": \"2020-01-01T13:00:00Z\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"value_exc_vat\": 12.62,\n" +
                        "            \"value_inc_vat\": 13.251,\n" +
                        "            \"valid_from\": \"2020-01-01T12:00:00Z\",\n" +
                        "            \"valid_to\": \"2020-01-01T12:30:00Z\"\n" +
                        "        }" +
                        "    ]" +
                        "}"));

        // When...
        UnitRatesResponse ratesFromNow = octopusClient.getRatesFromNow();

        // Then...
        assertThat(ratesFromNow.getResults()).hasSize(2);

        UnitRate firstRate = ratesFromNow.getResults().get(0);
        assertThat(firstRate.getValueIncVat()).isEqualTo("13.251");
        assertThat(firstRate.getValidFrom()).isEqualTo("2020-01-01T12:00:00Z");
        assertThat(firstRate.getValidTo()).isEqualTo("2020-01-01T12:30:00Z");

        UnitRate secondRate = ratesFromNow.getResults().get(1);
        assertThat(secondRate.getValueIncVat()).isEqualTo("9.24");
        assertThat(secondRate.getValidFrom()).isEqualTo("2020-01-01T12:30:00Z");
        assertThat(secondRate.getValidTo()).isEqualTo("2020-01-01T13:00:00Z");
    }
}