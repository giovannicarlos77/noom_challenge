package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.constants.MessageConstants
import com.noom.interview.fullstack.sleep.dto.ApiResponse
import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepStatistics
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.service.SleepLogService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/users/{userId}/sleep")
@CrossOrigin(origins = ["*"])
class SleepLogController(private val sleepLogService: SleepLogService) {

    @PostMapping
    fun createSleepLog(
        @PathVariable userId: Long,
        @Valid @RequestBody request: CreateSleepLogRequest
    ): ResponseEntity<ApiResponse<SleepLog>> {
        val sleepLog = sleepLogService.createSleepLog(userId, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(sleepLog, MessageConstants.SLEEP_LOG_CREATED_SUCCESSFULLY))
    }

    @GetMapping("/last-night")
    fun getLastNightSleep(@PathVariable userId: Long): ResponseEntity<ApiResponse<SleepLog>> {
        val sleepLog = sleepLogService.getLastNightSleep(userId)
        return if (sleepLog != null) {
            ResponseEntity.ok(ApiResponse.success(sleepLog))
        } else {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(MessageConstants.RESOURCE_NOT_FOUND))
        }
    }

    @GetMapping("/statistics/30-days")
    fun getLast30DaysStatistics(@PathVariable userId: Long): ResponseEntity<ApiResponse<SleepStatistics>> {
        val statistics = sleepLogService.getLast30DaysStatistics(userId)
        return if (statistics != null) {
            ResponseEntity.ok(ApiResponse.success(statistics))
        } else {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(MessageConstants.RESOURCE_NOT_FOUND))
        }
    }
}


