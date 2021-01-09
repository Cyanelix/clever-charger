package com.cyanelix.chargetimer.octopus;

import com.cyanelix.chargetimer.config.OctopusClientConfig;
import com.cyanelix.chargetimer.octopus.model.UnitRatesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OctopusClient {
    private final RestTemplate restTemplate;
    private final OctopusClientConfig octopusClientConfig;
    private final Clock clock;

    @Autowired
    public OctopusClient(RestTemplate restTemplate, OctopusClientConfig octopusClientConfig, Clock clock) {
        this.restTemplate = restTemplate;
        this.octopusClientConfig = octopusClientConfig;
        this.clock = clock;
    }

    public UnitRatesResponse getRatesFromNow() {
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromHttpUrl(
                        octopusClientConfig.getBaseUrl() + "/v1/products/AGILE-18-02-21/electricity-tariffs/E-1R-AGILE-18-02-21-C/standard-unit-rates/")
                        .queryParam("period_from", ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_DATE_TIME));

        return restTemplate.getForEntity(uriBuilder.toUriString(), UnitRatesResponse.class)
                .getBody();
    }
}
