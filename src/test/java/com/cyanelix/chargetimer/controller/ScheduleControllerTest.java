package com.cyanelix.chargetimer.controller;

import com.cyanelix.chargetimer.charges.RequiredChargesRepository;
import com.cyanelix.chargetimer.microtypes.ChargeLevel;
import com.cyanelix.chargetimer.microtypes.WeeklyTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ScheduleController.class)
class ScheduleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequiredChargesRepository requiredChargesRepository;

    @Test
    void noWeekliesNoExceptions_getSchedules_returnEmptyResponse() throws Exception {
        mockMvc.perform(
                get("/schedules"))
                .andExpect(status().isOk())
                .andExpect(content().json("{'weekly': {}, 'exceptions': {}}"));
    }

    @Test
    void twoWeekliesTwoExceptions_getSchedules_returnAll() throws Exception {
        Map<WeeklyTime, ChargeLevel> weeklies = new TreeMap<>();
        weeklies.put(new WeeklyTime(DayOfWeek.MONDAY, LocalTime.NOON), ChargeLevel.of(50));
        weeklies.put(new WeeklyTime(DayOfWeek.TUESDAY, LocalTime.MIDNIGHT), ChargeLevel.of(100));

        Map<ZonedDateTime, ChargeLevel> exceptions = new TreeMap<>();
        exceptions.put(ZonedDateTime.parse("2020-01-01T12:00:00Z"), ChargeLevel.of(10));
        exceptions.put(ZonedDateTime.parse("2020-02-02T22:00:00Z"), ChargeLevel.of(20));

        given(requiredChargesRepository.getWeeklySchedule()).willReturn(weeklies);
        given(requiredChargesRepository.getExceptions()).willReturn(exceptions);

        mockMvc.perform(
                get("/schedules"))
                .andExpect(status().isOk())
                .andExpect(content().json("{" +
                        "'weekly': {" +
                        "   'MONDAY @ 12:00': {" +
                        "       'value': 50" +
                        "   }," +
                        "   'TUESDAY @ 00:00': {" +
                        "       'value': 100" +
                        "   }" +
                        "}," +
                        "'exceptions': {" +
                        "   '2020-01-01T12:00:00Z': {" +
                        "       'value': 10" +
                        "   }," +
                        "   '2020-02-02T22:00:00Z': {" +
                        "       'value': 20" +
                        "   }" +
                        "}" +
                        "}"));
    }

    @Test
    void someWeeklies_init_returnConflict() throws Exception {
        Map<WeeklyTime, ChargeLevel> weeklies = new TreeMap<>();
        weeklies.put(new WeeklyTime(DayOfWeek.MONDAY, LocalTime.NOON), ChargeLevel.of(50));
        weeklies.put(new WeeklyTime(DayOfWeek.TUESDAY, LocalTime.MIDNIGHT), ChargeLevel.of(100));

        given(requiredChargesRepository.getWeeklySchedule()).willReturn(weeklies);

        mockMvc.perform(
                post("/schedules/init"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Weekly schedules already exist"));
    }

    @Test
    void noWeeklies_init_createsWeekliesAndReturns() throws Exception {
        Map<WeeklyTime, ChargeLevel> weeklies = new TreeMap<>();
        weeklies.put(new WeeklyTime(DayOfWeek.MONDAY, LocalTime.NOON), ChargeLevel.of(50));
        weeklies.put(new WeeklyTime(DayOfWeek.TUESDAY, LocalTime.MIDNIGHT), ChargeLevel.of(100));

        given(requiredChargesRepository.getWeeklySchedule())
                // First return no required charges
                .willReturn(Collections.emptyMap())
                // Then assume some init happened and return some values
                .willReturn(weeklies);

        mockMvc.perform(
                post("/schedules/init"))
                .andExpect(status().isOk())
                .andExpect(content().json("{" +
                "'weekly': {" +
                "   'MONDAY @ 12:00': {" +
                "       'value': 50" +
                "   }," +
                "   'TUESDAY @ 00:00': {" +
                "       'value': 100" +
                "   }" +
                "}" +
                "}"));

        verify(requiredChargesRepository, times(3))
                .addWeekly(any(WeeklyTime.class), any(ChargeLevel.class));
    }
}