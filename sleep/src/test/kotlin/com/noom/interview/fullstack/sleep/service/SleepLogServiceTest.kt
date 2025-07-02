package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.exception.ResourceConflictException
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
        val request = CreateSleepLogRequest(
            sleepDate = LocalDate.now(),
            bedtime = LocalTime.of(22, 30),
            wakeTime = LocalTime.of(7, 0),
            morningFeeling = MorningFeeling.GOOD
        )

        val expectedSleepLog = SleepLog(
            id = 1L,
            userId = testUser.id!!,
            sleepDate = request.sleepDate,
            bedtime = request.bedtime,
            wakeTime = request.wakeTime,
            totalTimeInBedMinutes = 510, // 8.5 hours
            morningFeeling = request.morningFeeling
        )

        `when`(userService.validateUserExists(testUser.id!!)).thenReturn(Unit)
        `when`(sleepLogRepository.findByUserIdAndDate(testUser.id!!, request.sleepDate)).thenReturn(null)
        `when`(sleepLogRepository.save(any())).thenReturn(expectedSleepLog)

        // When
        val result = sleepLogService.createSleepLog(testUser.id!!, request)

        // Then
        assertEquals(expectedSleepLog.id, result.id)
        assertEquals(expectedSleepLog.userId, result.userId)
        assertEquals(expectedSleepLog.sleepDate, result.sleepDate)
        assertEquals(expectedSleepLog.totalTimeInBedMinutes, result.totalTimeInBedMinutes)
        assertEquals(expectedSleepLog.morningFeeling, result.morningFeeling)

        verify(userService).validateUserExists(testUser.id!!)
        verify(sleepLogRepository).findByUserIdAndDate(testUser.id!!, request.sleepDate)
        verify(sleepLogRepository).save(any())
    }

    @Test
    fun `should throw exception when user not found`() {
        // Given
        val request = CreateSleepLogRequest(
            sleepDate = LocalDate.now(),
            bedtime = LocalTime.of(22, 30),
            wakeTime = LocalTime.of(7, 0),
            morningFeeling = MorningFeeling.GOOD
        )

        `when`(userService.validateUserExists(testUser.id!!)).thenThrow(IllegalArgumentException("User with id ${testUser.id} not found"))

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            sleepLogService.createSleepLog(testUser.id!!, request)
        }

        assertEquals("User with id ${testUser.id} not found", exception.message)
        verify(userService).validateUserExists(testUser.id!!)
        verifyNoInteractions(sleepLogRepository)
    }

    @Test
    fun `should throw exception when sleep log already exists for date`() {
        // Given
        val request = CreateSleepLogRequest(
            sleepDate = LocalDate.now(),
            bedtime = LocalTime.of(22, 30),
            wakeTime = LocalTime.of(7, 0),
            morningFeeling = MorningFeeling.GOOD
        )

        val existingSleepLog = SleepLog(
            id = 1L,
            userId = testUser.id!!,
            sleepDate = request.sleepDate,
            bedtime = LocalTime.of(23, 0),
            wakeTime = LocalTime.of(8, 0),
            totalTimeInBedMinutes = 540,
            morningFeeling = MorningFeeling.OK
        )

        `when`(userService.validateUserExists(testUser.id!!)).thenReturn(Unit)
        `when`(sleepLogRepository.findByUserIdAndDate(testUser.id!!, request.sleepDate)).thenReturn(existingSleepLog)

        // When & Then
        val exception = assertThrows(ResourceConflictException::class.java) {
            sleepLogService.createSleepLog(testUser.id!!, request)
        }

        assertEquals("Sleep log already exists for date ${request.sleepDate}", exception.message)
        verify(userService).validateUserExists(testUser.id!!)
        verify(sleepLogRepository).findByUserIdAndDate(testUser.id!!, request.sleepDate)
        verify(sleepLogRepository, never()).save(any(SleepLog::class.java))
    }

    @Test
    fun `should get last night sleep successfully`() {
        // Given
        val lastNightSleep = SleepLog(
            id = 1L,
            userId = testUser.id!!,
            sleepDate = LocalDate.now().minusDays(1),
            bedtime = LocalTime.of(22, 30),
            wakeTime = LocalTime.of(7, 0),
            totalTimeInBedMinutes = 510,
            morningFeeling = MorningFeeling.GOOD
        )

        `when`(userService.validateUserExists(testUser.id!!)).thenReturn(Unit)
        `when`(sleepLogRepository.findLastNightSleep(testUser.id!!)).thenReturn(lastNightSleep)

        // When
        val result = sleepLogService.getLastNightSleep(testUser.id!!)

        // Then
        assertNotNull(result)
        assertEquals(lastNightSleep.id, result!!.id)
        assertEquals(lastNightSleep.sleepDate, result.sleepDate)

        verify(userService).validateUserExists(testUser.id!!)
        verify(sleepLogRepository).findLastNightSleep(testUser.id!!)
    }

    @Test
    fun `should calculate time in bed correctly for same day`() {
        // Given
        val request = CreateSleepLogRequest(
            sleepDate = LocalDate.now(),
            bedtime = LocalTime.of(10, 0), // 10:00 AM
            wakeTime = LocalTime.of(18, 0), // 6:00 PM
            morningFeeling = MorningFeeling.GOOD
        )

        `when`(userService.validateUserExists(testUser.id!!)).thenReturn(Unit)
        `when`(sleepLogRepository.findByUserIdAndDate(testUser.id!!, request.sleepDate)).thenReturn(null)
        `when`(sleepLogRepository.save(any(SleepLog::class.java))).thenAnswer { invocation ->
            val sleepLog = invocation.getArgument<SleepLog>(0)
            sleepLog.copy(id = 1L)
        }

        // When
        val result = sleepLogService.createSleepLog(testUser.id!!, request)

        // Then
        assertEquals(480, result.totalTimeInBedMinutes) // 8 hours = 480 minutes
    }

    @Test
    fun `should calculate time in bed correctly for overnight sleep`() {
        // Given
        val request = CreateSleepLogRequest(
            sleepDate = LocalDate.now(),
            bedtime = LocalTime.of(23, 0), // 11:00 PM
            wakeTime = LocalTime.of(7, 0), // 7:00 AM next day
            morningFeeling = MorningFeeling.GOOD
        )

        `when`(userService.validateUserExists(testUser.id!!)).thenReturn(Unit)
        `when`(sleepLogRepository.findByUserIdAndDate(testUser.id!!, request.sleepDate)).thenReturn(null)
        `when`(sleepLogRepository.save(any(SleepLog::class.java))).thenAnswer { invocation ->
            val sleepLog = invocation.getArgument<SleepLog>(0)
            sleepLog.copy(id = 1L)
        }

        // When
        val result = sleepLogService.createSleepLog(testUser.id!!, request)

        // Then
        assertEquals(480, result.totalTimeInBedMinutes) // 8 hours = 480 minutes
    }
}

