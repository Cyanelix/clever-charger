package com.cyanelix.chargetimer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tesla.client")
public class TeslaClientConfig {
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String email;
    private String password;
    private String vin;
    private int wakeUpWaitSeconds;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public int getWakeUpWaitSeconds() {
        return wakeUpWaitSeconds;
    }

    public void setWakeUpWaitSeconds(int wakeUpWaitSeconds) {
        this.wakeUpWaitSeconds = wakeUpWaitSeconds;
    }
}
