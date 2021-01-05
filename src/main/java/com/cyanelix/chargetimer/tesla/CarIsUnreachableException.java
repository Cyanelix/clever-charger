package com.cyanelix.chargetimer.tesla;

public class CarIsUnreachableException extends RuntimeException {
    public CarIsUnreachableException(String message) {
        super(message);
    }
}
