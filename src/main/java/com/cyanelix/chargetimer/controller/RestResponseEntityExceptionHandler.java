package com.cyanelix.chargetimer.controller;

import com.cyanelix.chargetimer.controller.exception.SchedulesExistException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    // TODO: Suspect I can do better here by using the @ResponseStatus annotation in the exception
    @ExceptionHandler({SchedulesExistException.class})
    protected ResponseEntity<Object> handleSchedulesExistException(
            RuntimeException exception, WebRequest webRequest) {
        return handleExceptionInternal(exception, exception.getMessage(), new HttpHeaders(),
                HttpStatus.CONFLICT, webRequest);
    }
}
