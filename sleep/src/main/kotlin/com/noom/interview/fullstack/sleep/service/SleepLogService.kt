package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.DateRange
import com.noom.interview.fullstack.sleep.dto.SleepStatistics
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

@Service
class SleepLogService(
    private val sleepLogRepository: SleepLogRepository,
    private val userRepository: UserRepository
) {

    fun createSleepLog(userId: Long, request: CreateSleepLogRequest): SleepLog {
        // Verify user exists
        userRepository.findById(userId) 
            ?: throw IllegalArgumentException("User with id $userId not found")

        // Check if sleep log already exists for this date
        sleepLogRepository.findByUserIdAndDate(userId, request.sleepDate)?.let {
            throw IllegalArgumentException("Sleep log already exists for date ${request.sleepDate}")
        }

        // Calculate total time in bed
        val totalTimeInBed = calculateTimeInBed(request.bedtime, request.wakeTime)

        val sleepLog = SleepLog(
            userId = userId,
            sleepDate = request.sleepDate,
            bedtime = request.bedtime,
            wakeTime = request.wakeTime,
            totalTimeInBedMinutes = totalTimeInBed,
            morningFeeling = request.morningFeeling
        )

        return sleepLogRepository.save(sleepLog)
    }

    fun getLastNightSleep(userId: Long): SleepLog? {
        // Verify user exists
        userRepository.findById(userId) 
            ?: throw IllegalArgumentException("User with id $userId not found")

        return sleepLogRepository.findLastNightSleep(userId)
    }

    fun getLast30DaysStatistics(userId: Long): SleepStatistics? {
        // Verify user exists
        userRepository.findById(userId) 
            ?: throw IllegalArgumentException("User with id $userId not found")

        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(29) // 30 days including today

        val sleepLogs = sleepLogRepository.findByUserIdInDateRange(userId, startDate, endDate)
        
        if (sleepLogs.isEmpty()) {
            return null
        }

        val averageTimeInBed = sleepLogRepository.calculateAverageTimeInBed(userId, startDate, endDate) ?: 0.0
        val averageBedtime = sleepLogRepository.calculateAverageBedtime(userId, startDate, endDate) ?: LocalTime.MIDNIGHT
        val averageWakeTime = sleepLogRepository.calculateAverageWakeTime(userId, startDate, endDate) ?: LocalTime.MIDNIGHT
        val morningFeelingFrequencies = sleepLogRepository.getMorningFeelingFrequencies(userId, startDate, endDate)

        return SleepStatistics(
            dateRange = DateRange(startDate, endDate),
            averageTotalTimeInBedMinutes = averageTimeInBed,
            averageBedtime = averageBedtime,
            averageWakeTime = averageWakeTime,
            morningFeelingFrequencies = morningFeelingFrequencies
        )
    }

    private fun calculateTimeInBed(bedtime: LocalTime, wakeTime: LocalTime): Int {
        val bedtimeMinutes = bedtime.hour * 60 + bedtime.minute
        val wakeTimeMinutes = wakeTime.hour * 60 + wakeTime.minute
        
        return if (wakeTimeMinutes >= bedtimeMinutes) {
            // Same day
            wakeTimeMinutes - bedtimeMinutes
        } else {
            // Next day (crossed midnight)
            (24 * 60) - bedtimeMinutes + wakeTimeMinutes
        }
    }
}

