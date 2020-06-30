package com.cyanelix.chargetimer.tesla;

import org.springframework.stereotype.Component;

@Component
public class TeslaApiCache {
    private String authToken;
    private Long id;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean hasAuthToken() {
        return authToken != null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
