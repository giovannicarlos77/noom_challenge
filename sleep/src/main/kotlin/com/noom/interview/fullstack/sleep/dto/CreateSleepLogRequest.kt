package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.constants.MessageConstants
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent

data class CreateSleepLogRequest(
    @field:NotNull(message = MessageConstants.SLEEP_DATE_REQUIRED)
    @field:PastOrPresent(message = "Sleep date cannot be in the future")
    val sleepDate: LocalDate,
    
    @field:NotNull(message = MessageConstants.BEDTIME_REQUIRED)
    val bedtime: LocalTime,
    
    @field:NotNull(message = MessageConstants.WAKE_TIME_REQUIRED)
    val wakeTime: LocalTime,
    
    @field:NotNull(message = MessageConstants.MORNING_FEELING_REQUIRED)
    val morningFeeling: MorningFeeling
)


