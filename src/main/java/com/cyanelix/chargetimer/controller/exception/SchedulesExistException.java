package com.cyanelix.chargetimer.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class SchedulesExistException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Weekly schedules already exist";
    }
}
