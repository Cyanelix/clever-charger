package com.cyanelix.chargetimer.controller;

import com.cyanelix.chargetimer.charges.RequiredChargesRepository;
import com.cyanelix.chargetimer.controller.response.ScheduleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "schedules")
public class ScheduleController {
    private final RequiredChargesRepository requiredChargesRepository;

    @Autowired
    public ScheduleController(RequiredChargesRepository requiredChargesRepository) {
        this.requiredChargesRepository = requiredChargesRepository;
    }

    @GetMapping
    public ScheduleResponse getSchedule() {
        return new ScheduleResponse(
                requiredChargesRepository.getWeeklySchedule(),
                requiredChargesRepository.getExceptions());
    }
}
