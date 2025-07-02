package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.model.MorningFeeling
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.constraints.NotNull

data class CreateSleepLogRequest(
    @field:NotNull(message = "Sleep date cannot be null")
    val sleepDate: LocalDate,
    @field:NotNull(message = "Bedtime cannot be null")
    val bedtime: LocalTime,
    @field:NotNull(message = "Wake time cannot be null")
    val wakeTime: LocalTime,
    @field:NotNull(message = "Morning feeling cannot be null")
    val morningFeeling: MorningFeeling
)


