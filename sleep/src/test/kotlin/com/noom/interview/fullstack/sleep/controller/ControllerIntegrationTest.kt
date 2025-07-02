package com.noom.interview.fullstack.sleep.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.CreateUserRequest
import com.noom.interview.fullstack.sleep.model.MorningFeeling
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@ActiveProfiles("unittest")
class ControllerIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var mockMvc: MockMvc

    @Test
    fun `test all controllers integration`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        // Test TestController
        testTestController()

        // Test UserController
        val userId = testUserController()

        // Test SleepLogController
        testSleepLogController(userId)
    }

    private fun testTestController() {
        mockMvc.perform(get("/api/test"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.testMessage").value("Hello world!"))
    }

    private fun testUserController(): Long {
        val createUserRequest = CreateUserRequest(
            username = "testuser_integration",
            email = "test_integration@example.com"
        )

        val response = mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.username").value("testuser_integration"))
            .andReturn()

        val responseBody = objectMapper.readTree(response.response.contentAsString)
        return responseBody.get("data").get("id").asLong()
    }

    private fun testSleepLogController(userId: Long) {
        val createSleepLogRequest = CreateSleepLogRequest(
            sleepDate = LocalDate.now(),
            bedtime = LocalTime.of(22, 30),
            wakeTime = LocalTime.of(7, 0),
            morningFeeling = MorningFeeling.GOOD
        )

        // Test creating sleep log
        val response = mockMvc.perform(
            post("/api/users/$userId/sleep")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createSleepLogRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(userId))
            .andReturn()

        val responseBody = objectMapper.readTree(response.response.contentAsString)
        val sleepLogId = responseBody.get("data").get("id").asLong()

        // Test getting last night sleep
        mockMvc.perform(get("/api/users/$userId/sleep/last-night"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(sleepLogId))

        // Test getting statistics (might return 404 if no data in range, which is expected)
        mockMvc.perform(get("/api/users/$userId/sleep/statistics/30-days"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }
} 