package com.cyanelix.chargetimer.controller;

import com.cyanelix.chargetimer.charges.RequiredChargesRepository;
import com.cyanelix.chargetimer.controller.exception.SchedulesExistException;
import com.cyanelix.chargetimer.controller.response.ScheduleResponse;
import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.WeeklyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

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

    @PostMapping(path = "init")
    public ScheduleResponse initialiseSchedule() {
        Map<WeeklyTime, ChargeLevel> weeklySchedule = requiredChargesRepository.getWeeklySchedule();
        if (!weeklySchedule.isEmpty()) {
            throw new SchedulesExistException();
        }

        requiredChargesRepository.addWeekly(
                new WeeklyTime(DayOfWeek.MONDAY, LocalTime.of(6, 0)),
                ChargeLevel.of(70));
        requiredChargesRepository.addWeekly(
                new WeeklyTime(DayOfWeek.TUESDAY, LocalTime.of(6, 0)),
                ChargeLevel.of(70));
        requiredChargesRepository.addWeekly(
                new WeeklyTime(DayOfWeek.THURSDAY, LocalTime.of(6, 0)),
                ChargeLevel.of(70));

        return getSchedule();
    }
}
