package com.noom.interview.fullstack.sleep.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.noom.interview.fullstack.sleep.dto.CreateUserRequest
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate
import java.time.LocalTime

@Disabled("This test requires a database connection - run as integration test")
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("unittest")
@Transactional
class ControllerIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var sleepLogRepository: SleepLogRepository

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        objectMapper = ObjectMapper()
    }

    @Test
    fun `should create user successfully`() {
        // Given
        val request = CreateUserRequest(
            username = "testuser",
            email = "test@example.com"
        )

        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("testuser"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
    }

    @Test
    fun `should create sleep log successfully`() {
        // Given
        val user = userRepository.save(
            com.noom.interview.fullstack.sleep.model.User(
                username = "testuser",
                email = "test@example.com"
            )
        )

        val sleepLogRequest = com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest(
            sleepDate = LocalDate.now(),
            bedtime = LocalTime.of(22, 30),
            wakeTime = LocalTime.of(7, 0),
            morningFeeling = MorningFeeling.GOOD
        )

        // When & Then
        mockMvc.perform(
            post("/api/users/${user.id}/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sleepLogRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(user.id))
            .andExpect(jsonPath("$.data.sleepDate").exists())
    }

    @Test
    fun `should get last night sleep successfully`() {
        // Given
        val user = userRepository.save(
            com.noom.interview.fullstack.sleep.model.User(
                username = "testuser",
                email = "test@example.com"
            )
        )

        val sleepLog = sleepLogRepository.save(
            com.noom.interview.fullstack.sleep.model.SleepLog(
                userId = user.id!!,
                sleepDate = LocalDate.now().minusDays(1),
                bedtime = LocalTime.of(22, 30),
                wakeTime = LocalTime.of(7, 0),
                totalTimeInBedMinutes = 510,
                morningFeeling = MorningFeeling.GOOD
            )
        )

        // When & Then
        mockMvc.perform(
            get("/api/users/${user.id}/sleep-logs/last-night")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(sleepLog.id))
            .andExpect(jsonPath("$.data.userId").value(user.id))
    }

    @Test
    fun `should get sleep statistics successfully`() {
        // Given
        val user = userRepository.save(
            com.noom.interview.fullstack.sleep.model.User(
                username = "testuser",
                email = "test@example.com"
            )
        )

        // When & Then
        mockMvc.perform(
            get("/api/users/${user.id}/sleep-statistics")
                .param("startDate", LocalDate.now().minusDays(30).toString())
                .param("endDate", LocalDate.now().toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }
} 