package com.noom.interview.fullstack.sleep.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

data class SleepLog(
    val id: Long? = null,
    val userId: Long,
    val sleepDate: LocalDate,
    val bedtime: LocalTime,
    val wakeTime: LocalTime,
    val totalTimeInBedMinutes: Int,
    val morningFeeling: MorningFeeling,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)

