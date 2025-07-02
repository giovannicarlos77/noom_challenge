package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.model.MorningFeeling
import java.time.LocalDate
import java.time.LocalTime

data class SleepStatistics(
    val dateRange: DateRange,
    val averageTotalTimeInBedMinutes: Double,
    val averageBedtime: LocalTime,
    val averageWakeTime: LocalTime,
    val morningFeelingFrequencies: Map<MorningFeeling, Int>
)

data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
)

