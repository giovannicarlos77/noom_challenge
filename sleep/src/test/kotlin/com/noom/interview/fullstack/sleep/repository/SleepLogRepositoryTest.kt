package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

@Disabled("This test requires a database connection - run as integration test")
@SpringBootTest
@ActiveProfiles("unittest")
@Transactional
class SleepLogRepositoryTest {

    @Autowired
    private lateinit var sleepLogRepository: SleepLogRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser = userRepository.save(
            User(
                username = "sleepuser",
                email = "sleep@example.com"
            )
        )
    }

    @Test
    fun `should save and find sleep log by id`() {
        // Given
        val sleepLog = SleepLog(
            userId = testUser.id!!,
            sleepDate = LocalDate.now(),
            bedtime = LocalTime.of(22, 30),
            wakeTime = LocalTime.of(7, 0),
            totalTimeInBedMinutes = 510, // 8.5 hours
            morningFeeling = MorningFeeling.GOOD
        )

        // When
        val savedSleepLog = sleepLogRepository.save(sleepLog)

        // Then
        assertNotNull(savedSleepLog.id)
        assertEquals(testUser.id, savedSleepLog.userId)
        assertEquals(LocalDate.now(), savedSleepLog.sleepDate)
        assertEquals(LocalTime.of(22, 30), savedSleepLog.bedtime)
        assertEquals(LocalTime.of(7, 0), savedSleepLog.wakeTime)
        assertEquals(510, savedSleepLog.totalTimeInBedMinutes)
        assertEquals(MorningFeeling.GOOD, savedSleepLog.morningFeeling)

        // Verify we can find it by ID
        val foundSleepLog = sleepLogRepository.findById(savedSleepLog.id!!)
        assertNotNull(foundSleepLog)
        assertEquals(savedSleepLog.userId, foundSleepLog!!.userId)
    }

    @Test
    fun `should find sleep log by user id and date`() {
        // Given
        val date = LocalDate.now().minusDays(1)
        val sleepLog = SleepLog(
            userId = testUser.id!!,
            sleepDate = date,
            bedtime = LocalTime.of(23, 0),
            wakeTime = LocalTime.of(8, 0),
            totalTimeInBedMinutes = 540,
            morningFeeling = MorningFeeling.OK
        )
        val savedSleepLog = sleepLogRepository.save(sleepLog)

        // When
        val foundSleepLog = sleepLogRepository.findByUserIdAndDate(testUser.id!!, date)

        // Then
        assertNotNull(foundSleepLog)
        assertEquals(savedSleepLog.id, foundSleepLog!!.id)
        assertEquals(date, foundSleepLog.sleepDate)
    }

    @Test
    fun `should find last night sleep`() {
        // Given
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        // Create multiple sleep logs
        sleepLogRepository.save(
            SleepLog(
                userId = testUser.id!!,
                sleepDate = twoDaysAgo,
                bedtime = LocalTime.of(22, 0),
                wakeTime = LocalTime.of(6, 30),
                totalTimeInBedMinutes = 510,
                morningFeeling = MorningFeeling.BAD
            )
        )

        val lastNightSleep = sleepLogRepository.save(
            SleepLog(
                userId = testUser.id!!,
                sleepDate = yesterday,
                bedtime = LocalTime.of(23, 30),
                wakeTime = LocalTime.of(7, 30),
                totalTimeInBedMinutes = 480,
                morningFeeling = MorningFeeling.GOOD
            )
        )

        // When
        val foundLastNight = sleepLogRepository.findLastNightSleep(testUser.id!!)

        // Then
        assertNotNull(foundLastNight)
        assertEquals(lastNightSleep.id, foundLastNight!!.id)
        assertEquals(yesterday, foundLastNight.sleepDate)
    }

    @Test
    fun `should calculate average time in bed`() {
        // Given
        val startDate = LocalDate.now().minusDays(2)
        val endDate = LocalDate.now()

        sleepLogRepository.save(
            SleepLog(
                userId = testUser.id!!,
                sleepDate = startDate,
                bedtime = LocalTime.of(22, 0),
                wakeTime = LocalTime.of(6, 0),
                totalTimeInBedMinutes = 480, // 8 hours
                morningFeeling = MorningFeeling.GOOD
            )
        )

        sleepLogRepository.save(
            SleepLog(
                userId = testUser.id!!,
                sleepDate = startDate.plusDays(1),
                bedtime = LocalTime.of(23, 0),
                wakeTime = LocalTime.of(7, 0),
                totalTimeInBedMinutes = 480, // 8 hours
                morningFeeling = MorningFeeling.OK
            )
        )

        // When
        val averageTime = sleepLogRepository.calculateAverageTimeInBed(testUser.id!!, startDate, endDate)

        // Then
        assertNotNull(averageTime)
        assertEquals(480.0, averageTime!!, 0.1)
    }

    @Test
    fun `should get morning feeling frequencies`() {
        // Given
        val startDate = LocalDate.now().minusDays(3)
        val endDate = LocalDate.now()

        sleepLogRepository.save(
            SleepLog(
                userId = testUser.id!!,
                sleepDate = startDate,
                bedtime = LocalTime.of(22, 0),
                wakeTime = LocalTime.of(6, 0),
                totalTimeInBedMinutes = 480,
                morningFeeling = MorningFeeling.GOOD
            )
        )

        sleepLogRepository.save(
            SleepLog(
                userId = testUser.id!!,
                sleepDate = startDate.plusDays(1),
                bedtime = LocalTime.of(23, 0),
                wakeTime = LocalTime.of(7, 0),
                totalTimeInBedMinutes = 480,
                morningFeeling = MorningFeeling.GOOD
            )
        )

        sleepLogRepository.save(
            SleepLog(
                userId = testUser.id!!,
                sleepDate = startDate.plusDays(2),
                bedtime = LocalTime.of(0, 0),
                wakeTime = LocalTime.of(8, 0),
                totalTimeInBedMinutes = 480,
                morningFeeling = MorningFeeling.BAD
            )
        )

        // When
        val frequencies = sleepLogRepository.getMorningFeelingFrequencies(testUser.id!!, startDate, endDate)

        // Then
        assertEquals(2, frequencies[MorningFeeling.GOOD])
        assertEquals(1, frequencies[MorningFeeling.BAD])
        assertNull(frequencies[MorningFeeling.OK])
    }
}

