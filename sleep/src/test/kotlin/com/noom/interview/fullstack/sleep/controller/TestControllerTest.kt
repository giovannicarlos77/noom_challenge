package com.noom.interview.fullstack.sleep.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(TestController::class)
class TestControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `test endpoint should return hello world message`() {
        mockMvc.perform(get("/api/test"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.testMessage").value("Hello world!"))
    }

    @Test
    fun `test endpoint should return correct response structure`() {
        mockMvc.perform(get("/api/test"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isMap)
            .andExpect(jsonPath("$.testMessage").exists())
            .andExpect(jsonPath("$.testMessage").isString)
    }

    @Test
    fun `test endpoint should handle request correctly`() {
        val result = mockMvc.perform(get("/api/test"))
            .andExpect(status().isOk)
            .andReturn()

        val responseBody = result.response.contentAsString
        assert(responseBody.contains("Hello world!"))
        assert(responseBody.contains("testMessage"))
    }
} 