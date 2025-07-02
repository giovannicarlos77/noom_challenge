package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.exception.ResourceConflictException
import com.noom.interview.fullstack.sleep.exception.ResourceNotFoundException
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class SleepLogServiceTest {

    @Mock
    private lateinit var sleepLogRepository: SleepLogRepository

    @Mock
    private lateinit var userService: UserService

    private lateinit var sleepLogService: SleepLogService

    private val testUser = User(
        id = 1L,
        username = "testuser",
        email = "test@example.com"
    )

    @BeforeEach
    fun setUp() {
        sleepLogService = SleepLogService(sleepLogRepository, userService)
    }

    @Test
    fun `should create sleep log successfully`() {
        // Given
        val userId = 1L
        val request = CreateSleepLogRequest(
            sleepDate = LocalDate.of(2025, 1, 1),
            bedtime = LocalTime.of(22, 30),
            wakeTime = LocalTime.of(7, 0),
            morningFeeling = MorningFeeling.GOOD
        )

        val expectedSleepLog = SleepLog(
            id = 1L,
            userId = userId,
            sleepDate = request.sleepDate,
            bedtime = request.bedtime,
            wakeTime = request.wakeTime,
            totalTimeInBedMinutes = 510,
            morningFeeling = request.morningFeeling
        )

        `when`(userService.validateUserExists(userId)).thenReturn(testUser)
        `when`(sleepLogRepository.findByUserIdAndDate(userId, request.sleepDate)).thenReturn(null)
        `when`(sleepLogRepository.save(any(SleepLog::class.java))).thenReturn(expectedSleepLog)

        // When
        val result = sleepLogService.createSleepLog(userId, request)

        // Then
        assertEquals(expectedSleepLog.id, result.id)
        assertEquals(expectedSleepLog.userId, result.userId)
        assertEquals(expectedSleepLog.sleepDate, result.sleepDate)
        assertEquals(expectedSleepLog.totalTimeInBedMinutes, result.totalTimeInBedMinutes)
        assertEquals(expectedSleepLog.morningFeeling, result.morningFeeling)

        verify(userService).validateUserExists(userId)
        verify(sleepLogRepository).findByUserIdAndDate(userId, request.sleepDate)
        verify(sleepLogRepository).save(any(SleepLog::class.java))
    }

    @Test
    fun `should throw exception when user not found`() {
        // Given
        val userId = 1L
        val request = CreateSleepLogRequest(
            sleepDate = LocalDate.of(2025, 1, 1),
            bedtime = LocalTime.of(22, 30),
            wakeTime = LocalTime.of(7, 0),
            morningFeeling = MorningFeeling.GOOD
        )

        `when`(userService.validateUserExists(userId)).thenThrow(ResourceNotFoundException("User with id $userId not found"))

        // When & Then
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            sleepLogService.createSleepLog(userId, request)
        }

        assertEquals("User with id $userId not found", exception.message)
        verify(userService).validateUserExists(userId)
        verifyNoInteractions(sleepLogRepository)
    }

    @Test
    fun `should throw exception when sleep log already exists for date`() {
        // Given
        val userId = 1L
        val request = CreateSleepLogRequest(
            sleepDate = LocalDate.of(2025, 1, 1),
            bedtime = LocalTime.of(22, 30),
            wakeTime = LocalTime.of(7, 0),
            morningFeeling = MorningFeeling.GOOD
        )

        val existingSleepLog = SleepLog(
            id = 1L,
            userId = userId,
            sleepDate = request.sleepDate,
            bedtime = LocalTime.of(23, 0),
            wakeTime = LocalTime.of(8, 0),
            totalTimeInBedMinutes = 540,
            morningFeeling = MorningFeeling.OK
        )

        `when`(userService.validateUserExists(userId)).thenReturn(testUser)
        `when`(sleepLogRepository.findByUserIdAndDate(userId, request.sleepDate)).thenReturn(existingSleepLog)

        // When & Then
        val exception = assertThrows(ResourceConflictException::class.java) {
            sleepLogService.createSleepLog(userId, request)
        }

        assertTrue(exception.message!!.contains("Sleep log already exists for date"))
        verify(userService).validateUserExists(userId)
        verify(sleepLogRepository).findByUserIdAndDate(userId, request.sleepDate)
        verify(sleepLogRepository, never()).save(any(SleepLog::class.java))
    }

    @Test
    fun `should get last night sleep successfully`() {
        // Given
        val userId = 1L
        val lastNightSleep = SleepLog(
            id = 1L,
            userId = userId,
            sleepDate = LocalDate.now().minusDays(1),
            bedtime = LocalTime.of(22, 30),
            wakeTime = LocalTime.of(7, 0),
            totalTimeInBedMinutes = 510,
            morningFeeling = MorningFeeling.GOOD
        )

        `when`(userService.validateUserExists(userId)).thenReturn(testUser)
        `when`(sleepLogRepository.findLastNightSleep(userId)).thenReturn(lastNightSleep)

        // When
        val result = sleepLogService.getLastNightSleep(userId)

        // Then
        assertNotNull(result)
        assertEquals(lastNightSleep.id, result!!.id)
        assertEquals(lastNightSleep.sleepDate, result.sleepDate)

        verify(userService).validateUserExists(userId)
        verify(sleepLogRepository).findLastNightSleep(userId)
    }

    @Test
    fun `should calculate time in bed correctly for same day`() {
        // Given
        val userId = 1L
        val request = CreateSleepLogRequest(
            sleepDate = LocalDate.of(2025, 1, 1),
            bedtime = LocalTime.of(10, 0), // 10:00 AM
            wakeTime = LocalTime.of(18, 0), // 6:00 PM
            morningFeeling = MorningFeeling.GOOD
        )

        `when`(userService.validateUserExists(userId)).thenReturn(testUser)
        `when`(sleepLogRepository.findByUserIdAndDate(userId, request.sleepDate)).thenReturn(null)
        `when`(sleepLogRepository.save(any(SleepLog::class.java))).thenAnswer { invocation ->
            val sleepLog = invocation.getArgument<SleepLog>(0)
            sleepLog.copy(id = 1L)
        }

        // When
        val result = sleepLogService.createSleepLog(userId, request)

        // Then
        assertEquals(480, result.totalTimeInBedMinutes) // 8 hours = 480 minutes
    }

    @Test
    fun `should calculate time in bed correctly for overnight sleep`() {
        // Given
        val userId = 1L
        val request = CreateSleepLogRequest(
            sleepDate = LocalDate.of(2025, 1, 1),
            bedtime = LocalTime.of(23, 0), // 11:00 PM
            wakeTime = LocalTime.of(7, 0), // 7:00 AM next day
            morningFeeling = MorningFeeling.GOOD
        )

        `when`(userService.validateUserExists(userId)).thenReturn(testUser)
        `when`(sleepLogRepository.findByUserIdAndDate(userId, request.sleepDate)).thenReturn(null)
        `when`(sleepLogRepository.save(any(SleepLog::class.java))).thenAnswer { invocation ->
            val sleepLog = invocation.getArgument<SleepLog>(0)
            sleepLog.copy(id = 1L)
        }

        // When
        val result = sleepLogService.createSleepLog(userId, request)

        // Then
        assertEquals(480, result.totalTimeInBedMinutes) // 8 hours = 480 minutes
    }
}

