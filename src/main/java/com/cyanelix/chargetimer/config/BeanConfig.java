package com.cyanelix.chargetimer.config;

import com.cyanelix.chargetimer.electricity.FlexibleOctopusTariff;
import com.cyanelix.chargetimer.electricity.Tariff;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

@Configuration
public class BeanConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public Tariff tariff() {
        return new FlexibleOctopusTariff();
    }
}
