package com.cyanelix.chargetimer.tesla;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccessTokenRequest {
    private final String grantType = "password";
    private final String clientId;
    private final String clientSecret;
    private final String email;
    private final String password;

    public AccessTokenRequest(String clientId, String clientSecret, String email, String password) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.email = email;
        this.password = password;
    }

    public String getGrantType() {
        return grantType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
